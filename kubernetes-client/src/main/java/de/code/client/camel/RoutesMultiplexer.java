package de.code.client.camel;

import de.code.client.common.model.Event;
import de.code.client.kubernetes.cache.SubscriptionCache;
import de.code.client.kubernetes.resources.SubscriptionResource;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.code.client.common.model.Constants.NETTY_PREFIX;
import static org.apache.camel.component.kafka.KafkaConstants.TOPIC;


public class RoutesMultiplexer {

    private static final Logger logger = LoggerFactory.getLogger(RoutesMultiplexer.class);

    TriggerFilter triggerFilter;
    SubscriptionCache subscriptionCache;
    String topic;

    public RoutesMultiplexer(SubscriptionCache subscriptionCache, String topic, TriggerFilter triggerFilter) {
        this.subscriptionCache = subscriptionCache;
        this.topic = topic;
        this.triggerFilter = triggerFilter;
    }

    public Set<String> getSubscriptions(Exchange exchange) {
        logger.info("topic = {}, exchange = {}, subscriptionRegistry = {}", topic, exchange, subscriptionCache);
        exchange.setProperty(TOPIC, topic);
        List<SubscriptionResource> potentialSubscribers = subscriptionCache.getSubscriptionsForTopic(topic);
        Event event = exchange.getMessage().getBody(Event.class);
        Set<String> subscriptions = potentialSubscribers.stream()
                .filter(subscription -> triggerFilter.applies(subscription.getSpec().getSubscription(), event))
                .map(subscription ->
                        NETTY_PREFIX + subscription.getSpec().getSubscription().getCallback() +
                                ";subscriptionId="+subscription.getSpec().getSubscription().getSubscriptionId()).collect(Collectors.toSet());
        logger.info("found subscriptions: {}", subscriptions);
        return subscriptions;
    }
}
