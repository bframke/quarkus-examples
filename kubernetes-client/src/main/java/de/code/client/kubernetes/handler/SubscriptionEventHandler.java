package de.code.client.kubernetes.handler;

import de.code.client.kubernetes.cache.SubscriptionCache;
import de.code.client.kubernetes.resources.SubscriptionResourceSpecification;
import io.fabric8.kubernetes.client.Watcher;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class SubscriptionEventHandler {

    Logger logger = LoggerFactory.getLogger(SubscriptionEventHandler.class);

    @Inject
    SubscriptionCache subscriptionCache;

    void onStartup(@Observes StartupEvent event) {
        new Thread(this::runWatch).start();
    }

    void runWatch() {
        subscriptionCache.list(this::handleEvent);
        subscriptionCache.watch(this::handleEvent);
    }

    void handleEvent(Watcher.Action action, SubscriptionResourceSpecification subscriptionResourceSpecification) {
        logger.info("Event received {} with subscription with id {}", action, subscriptionResourceSpecification.getSubscription().getSubscriptionId());
    }
}
