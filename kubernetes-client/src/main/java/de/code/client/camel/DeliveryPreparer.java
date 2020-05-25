package de.code.client.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.code.client.common.model.Event;
import de.code.client.kubernetes.cache.SubscriptionCache;
import de.code.client.kubernetes.resources.SubscriptionResource;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

import static de.code.client.common.model.Constants.NETTY_PREFIX;
import static org.apache.camel.Exchange.TO_ENDPOINT;
import static org.apache.camel.component.kafka.KafkaConstants.TOPIC;
import static org.hibernate.validator.internal.util.StringHelper.isNullOrEmptyString;

@ApplicationScoped
public class DeliveryPreparer implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryPreparer.class);
    private static final String SUBSCRIPTION_ID = ";subscriptionId=";

    @Inject
    ObjectMapper objectMapper;
    @Inject
    SubscriptionCache subscriptionCache;

    @Override
    public void process(Exchange exchange) {
        String topic = (String)exchange.getProperty(TOPIC);
        String endpoint = (String)exchange.getProperty(TO_ENDPOINT);
        if (! isNullOrEmptyString(endpoint)) {
            int subscriptionIdIndex = endpoint.indexOf(SUBSCRIPTION_ID)+SUBSCRIPTION_ID.length();
            String subscriptionId = endpoint.substring(subscriptionIdIndex);
            logger.info("preparing message for subscription: {}", subscriptionId);
            Optional<SubscriptionResource> subscription = subscriptionCache.getSubscriptionForTopicAndId(topic, subscriptionId);
	    subscription.ifPresent( subscriptionResource -> {
                String newEndpoint = NETTY_PREFIX + subscriptionResource.getSpec().getSubscription().getCallback();
                logger.info("setting new endpoint: {}", newEndpoint);
                exchange.setProperty(TO_ENDPOINT, newEndpoint);
                logger.info("prop");
                Message message = exchange.getMessage();
                Event event = message.getBody(Event.class);
                logger.info("received event with type={}, specVersion={}, source={}", event.getType(), event.getSpecversion(), event.getSource());
                event.setData(null);
                try {
                    message.setBody(objectMapper.writeValueAsString(event));
                } catch (Exception e) {
                    logger.error("could not serialize event", e);
                    message.setBody(event.toString());
                }
            });
        } else {
            logger.warn("no destination URL set");
        }
    }
}
