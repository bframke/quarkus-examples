package de.code.client.camel;

import de.code.client.kubernetes.cache.SubscriptionCache;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MultiplexerFactory {
    @Inject
    SubscriptionCache subscriptionCache;
    @Inject
    TriggerFilter triggerFilter;

    public RoutesMultiplexer createMultiplexer(String topic) {
        return new RoutesMultiplexer(subscriptionCache, topic, triggerFilter);
    }

}
