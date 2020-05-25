package de.code.client.kubernetes.cache;

import de.code.client.camel.RoutesHandler;
import de.code.client.common.model.Holder;
import de.code.client.common.model.Problem;
import de.code.client.common.model.SubscriptionWithId;
import de.code.client.kubernetes.resources.SubscriptionResource;
import de.code.client.kubernetes.resources.SubscriptionResourceDoneable;
import de.code.client.kubernetes.resources.SubscriptionResourceList;
import de.code.client.kubernetes.resources.SubscriptionResourceSpecification;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.runtime.Quarkus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static io.fabric8.kubernetes.client.Watcher.Action.ADDED;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class SubscriptionCache {

    private final Logger logger = LoggerFactory.getLogger(SubscriptionCache.class);

    @Inject
    RoutesHandler routesHandler;

    @Inject
    NonNamespaceOperation<SubscriptionResource,
            SubscriptionResourceList,
            SubscriptionResourceDoneable,
            Resource<SubscriptionResource, SubscriptionResourceDoneable>> subscriptionResourceClient;

    ConcurrentHashMap<String, List<SubscriptionResource>> subscriptionResourceCache = new ConcurrentHashMap<>();
    private final Executor executor = Executors.newSingleThreadExecutor();

    public Map<String, List<SubscriptionResource>> getCache() {
        return subscriptionResourceCache;
    }

    public List<SubscriptionWithId> getAllSubscriptions(String subscriberId) {

        return subscriptionResourceCache.values().stream()
            .flatMap(List::stream)
            .filter(subscriptionResource -> subscriberId == null || subscriptionResource.getSpec().getSubscriberId().equals(subscriberId))
            .map(SubscriptionResource::getSpec)
            .map(SubscriptionResourceSpecification::getSubscription)
            .collect(toList());
    }

    public Optional<Problem> addSubscription(SubscriptionResource subscriptionResource) {
        String type = subscriptionResource.getSpec().getSubscription().getType();
        if (subscriptionIsUnique(type, subscriptionResource)) {
            subscriptionResourceCache.computeIfAbsent(type, x -> new CopyOnWriteArrayList<>()).add(subscriptionResource);
            return routesHandler.subscriptionAdded(subscriptionResource.getSpec().getSubscription());
        }
        subscriptionResource.getSpec().getSubscription().setSubscriptionId(findIdOfExistingSubscription(subscriptionResource));
        return empty();
    }

    public Optional<Problem> deleteSubscription(String subscriptionId, Holder<Boolean> success) {
        success.set(false);
        for (List<SubscriptionResource> subscriptionList : subscriptionResourceCache.values()) {
            Optional<SubscriptionResource> oldSubscription = subscriptionList.stream()
                    .filter(subscription -> subscriptionId.equals(subscription.getSpec().getSubscription().getSubscriptionId()))
                    .findFirst();
            if (oldSubscription.isPresent()) {
                subscriptionList.remove(oldSubscription.get());
                Optional<Problem> problem = routesHandler.subscriptionRemoved(oldSubscription.get().getSpec().getSubscription());
                if (problem.isPresent()) {
                    return problem;
                }
                success.set(true);
                break;
            }
        }
        return empty();
    }

    String findIdOfExistingSubscription(SubscriptionResource subscriptionResource) {
        final SubscriptionResourceSpecification spec = subscriptionResource.getSpec();
        final SubscriptionWithId subscription = spec.getSubscription();

        return subscriptionResourceCache.get(subscription.getType())
            .stream()
            .filter(entry -> entry.getSpec().equalsWithoutId(spec))
            .findFirst()
            .map(e -> e.getSpec().getSubscription().getSubscriptionId())
            .orElse("no-existing-id-found");
    }

    public List<SubscriptionResource> getSubscriptionsForTopic(String eventType) {
        if (!subscriptionResourceCache.containsKey(eventType)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(subscriptionResourceCache.get(eventType));
    }

    public Optional<SubscriptionResource> getSubscriptionForTopicAndId(String topic, String subscriptionId) {
        return filterSubscriptionResource(buildAndPredicate(subscriptionId, topic));
    }

    public Optional<SubscriptionResource> getSubscriptionForId(String subscriptionId) {
        return filterSubscriptionResource(buildAndPredicate(subscriptionId, null));
    }

    private Optional<SubscriptionResource> filterSubscriptionResource(Predicate<SubscriptionResource> predicates) {
        return subscriptionResourceCache.values()
            .stream()
            .flatMap(List::stream)
            .filter(predicates)
            .findFirst();
    }

    private Predicate<SubscriptionResource> buildAndPredicate(String subscriptionId, String topic) {

        List<Predicate<SubscriptionResource>> allPredicates = new ArrayList<>();

        if (subscriptionId != null) {
            allPredicates.add(subscriptionResource ->
                subscriptionId.equals(subscriptionResource.getSpec().getSubscription().getSubscriptionId()));
        }

        if (topic != null) {
            allPredicates.add(subscriptionResource ->
                topic.equals(subscriptionResource.getSpec().getSubscription().getType()));
        }

        return allPredicates.stream().reduce(predicate -> true, Predicate::and);
    }

    boolean subscriptionIsUnique(String eventType, SubscriptionResource subscriptionResource) {
        if (!subscriptionResourceCache.containsKey(eventType)) {
            return true;
        }

        final SubscriptionResourceSpecification subscriptionSpecification = subscriptionResource.getSpec();
        logger.info("check incoming {}", subscriptionSpecification.getSubscription().getSubscriptionId());

        boolean foundSame = subscriptionResourceCache.get(eventType)
                .stream()
                .map(SubscriptionResource::getSpec)
                .anyMatch(extendedSubscription -> {
                    if (subscriptionSpecification.equalsWithoutId(extendedSubscription)) {
                        logger.warn("subscription is a duplicate of existing one with id '{}'", subscriptionSpecification.getSubscription().getSubscriptionId());
                        return true;
                    }
                    return false;
                });
        return !foundSame;
    }

    public void list(BiConsumer<Watcher.Action, SubscriptionResourceSpecification> callback) {
        subscriptionResourceClient.list().getItems()
                .forEach(resource -> {
                    addSubscription(resource);
                    executor.execute(() -> callback.accept(ADDED, resource.getSpec()));
                });
    }

    public void watch(BiConsumer<Watcher.Action, SubscriptionResourceSpecification> callback) {
        subscriptionResourceClient.watch(new SubscriptionResourceWatcher(callback));
    }

    class SubscriptionResourceWatcher implements Watcher<SubscriptionResource> {
        private final BiConsumer<Watcher.Action, SubscriptionResourceSpecification> callback;

        SubscriptionResourceWatcher(BiConsumer<Watcher.Action, SubscriptionResourceSpecification> callback) {
            this.callback = callback;
        }

        @Override
        public void eventReceived(Watcher.Action action, SubscriptionResource resource) {
            boolean resourceExists = getSubscriptionForTopicAndId(resource.getSpec().getSubscription().getType(), resource.getSpec().getSubscription().getSubscriptionId()).isPresent();
            switch (action) {
                case ADDED:
                case MODIFIED:
                    logger.info("received {} for resource {}", action, resource);
                    if ( ! resourceExists) {
                        addSubscription(resource);
                        executor.execute(() -> callback.accept(action, resource.getSpec()));
                    } else {
                        logger.debug("skip adding for resource {}. Already exists.", resource);
                    }
                    break;
                case DELETED:
                    logger.info("received {} for resource {}", action, resource);
                    deleteSubscription(resource.getSpec().getSubscription().getSubscriptionId(), new Holder<>(false));
                    executor.execute(() -> callback.accept(action, resource.getSpec()));
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

}
