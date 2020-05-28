package kubernetes.client.handler;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.quarkus.runtime.Quarkus;
import kubernetes.client.cache.SubscriptionCache;
import kubernetes.client.common.Holder;
import kubernetes.client.custom.resource.SubscriptionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
class SubscriptionResourceWatcher implements Watcher<SubscriptionResource> {
    Logger logger = LoggerFactory.getLogger(SubscriptionResourceWatcher.class);

    @Inject
    SubscriptionCache subscriptionCache;

    @Override
    public void eventReceived(Action action, SubscriptionResource resource) {
        logger.info("received {} for resource {}", action, resource);
        boolean resourceExists = subscriptionCache.getSubscriptionForTopicAndId(resource.getSpec().getSubscription().getType(), resource.getSpec().getSubscription().getSubscriptionId()).isPresent();
        switch (action) {
            case ADDED:
                if (resourceExists) {
                    logger.debug("skip adding for resource {}. Already exists.", resource);
                    break;
                }
                subscriptionCache.addSubscription(resource);
                break;
            case MODIFIED:
                logger.info("where are you coming from?");
                break;
            case DELETED:
                subscriptionCache.deleteSubscription(resource.getSpec().getSubscription().getSubscriptionId(), new Holder<>(false));
                break;
            case ERROR:
            default:
                logger.error("Received unexpected {} event for {}", action, resource);
                quarkusExit();
                break;
        }
    }

    @Override
    public void onClose(KubernetesClientException cause) {
        logger.error(cause.getMessage());
        quarkusExit();
    }

    void quarkusExit() {
        Quarkus.asyncExit();
    }
}
