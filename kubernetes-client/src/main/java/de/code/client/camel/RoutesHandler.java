package de.code.client.camel;

import de.code.client.common.model.Problem;
import de.code.client.common.model.ProblemFactory;
import de.code.client.common.model.Subscription;
import de.code.client.common.model.SubscriptionWithId;
import de.code.client.config.SubscriberConfig;
import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.engine.AbstractCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@ApplicationScoped
public class RoutesHandler {
    private static final String KAFKA_URL_TEMPLATE = "kafka:%s?brokers=%s&groupId=horizon";
    private static final Logger logger = LoggerFactory.getLogger(RoutesHandler.class);

    @Inject
    SubscriberConfig subscriberConfig;
    @Inject
    CamelContext camelContext;
    @Inject
    KafkaBodyProcessor kafkaBodyProcessor;
    @Inject
    DeliveryPreparer deliveryPreparer;
    @Inject
    MultiplexerFactory multiplexerFactory;
    @Inject
    ProblemFactory problemFactory;

    final MultivaluedHashMap<String,String> subscriptionsForTopics = new MultivaluedHashMap<>();

    public Optional<Problem> subscriptionAdded(SubscriptionWithId subscription) {
        subscriptionsForTopics.add(subscription.getType(), subscription.getSubscriptionId());
        return startRouteIfNeeded(subscription);
    }


    public Optional<Problem>  subscriptionRemoved(SubscriptionWithId subscription) {
        String topic = subscription.getType();
        if ( ! subscriptionsForTopics.containsKey(topic)) {
            logger.warn("could not delete subscription for unknown type '{}'", topic);
            return empty();
        }
        List<String> subscriptionIds = subscriptionsForTopics.get(topic);
        if (subscriptionIds.remove(subscription.getSubscriptionId())) {
            if (subscriptionIds.isEmpty()) {
                logger.info("no more subscriptions for this topic ({}), stopping associated route", topic);
                try {
                    ((AbstractCamelContext)camelContext).stopRoute(topic);
                    camelContext.removeRoute(topic);
                } catch (Exception e) {
                    logger.error("error removing route", e);
                    return of(problemFactory.createProblem().status(INTERNAL_SERVER_ERROR.getStatusCode()).title(e.getMessage()));
                }
                logger.info("route stopped and deleted.");
            }
        } else {
            logger.warn("could not delete unknown subscription '{}'", subscription.getSubscriptionId());
        }
        return empty();
    }



    Optional<Problem> startRouteIfNeeded(Subscription subscription) {
        String topic = subscription.getType();
        if (camelContext.getRoute(topic) == null) {
            logger.info("no route from topic '{}' yet, creating a new one.", topic);
            RoutesMultiplexer routesMultiplexer = multiplexerFactory.createMultiplexer(topic);
            RoutesBuilder builder = new RouteBuilder() {
                @Override
                public void configure() {

                    from(String.format(KAFKA_URL_TEMPLATE, topic, subscriberConfig.getKafkaBrokers()))
                            .id(topic)
                            .log("Message received: ${headers}")
                            .process(kafkaBodyProcessor)
                            .log("Headers: ${headers}")
                            .recipientList()
                                .method(routesMultiplexer,"getSubscriptions")
				                .onPrepare(deliveryPreparer)
                                .parallelProcessing()
                            .log("finished");
                }
            };

            try {
                camelContext.addRoutes(builder);
                Thread.sleep(500);
            } catch (Exception e) {
                logger.error("error stating route", e);
                return of(problemFactory.createProblem().status(INTERNAL_SERVER_ERROR.getStatusCode()).title(e.getMessage()));
            }
            logger.info("route created.");
        } else {
            logger.info("reusing existing route from topic '{}'", topic);
        }
        return empty();
    }

}
