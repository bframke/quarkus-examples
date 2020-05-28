package kubernetes.client.handler;

import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.runtime.StartupEvent;
import kubernetes.client.cache.SubscriptionCache;
import kubernetes.client.custom.resource.SubscriptionResource;
import kubernetes.client.custom.resource.SubscriptionResourceDoneable;
import kubernetes.client.custom.resource.SubscriptionResourceList;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class SubscriptionEventHandler {

    @Inject
    SubscriptionCache subscriptionCache;

    @Inject
    SubscriptionResourceWatcher subscriptionResourceWatcher;

    @Inject
    NonNamespaceOperation<SubscriptionResource,
            SubscriptionResourceList,
            SubscriptionResourceDoneable,
            Resource<SubscriptionResource, SubscriptionResourceDoneable>> subscriptionResourceClient;

    void onStartup(@Observes StartupEvent event) {
        new Thread(this::runWatch).start();
    }

    void runWatch() {
        subscriptionResourceClient.list().getItems().forEach(subscriptionCache::addSubscription);
        subscriptionResourceClient.watch(subscriptionResourceWatcher);
    }

}
