package kubernetes.client.cache;

import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

import kubernetes.client.common.Holder;
import kubernetes.client.common.Problem;
import kubernetes.client.common.SubscriptionWithId;
import kubernetes.client.custom.resource.SubscriptionResource;
import kubernetes.client.custom.resource.SubscriptionResourceDoneable;
import kubernetes.client.custom.resource.SubscriptionResourceList;
import kubernetes.client.custom.resource.SubscriptionResourceSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import static java.util.Collections.*;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class SubscriptionCache {

    private final Logger logger = LoggerFactory.getLogger(SubscriptionCache.class);

    @Inject
    NonNamespaceOperation<SubscriptionResource,
            SubscriptionResourceList,
            SubscriptionResourceDoneable,
                Resource<SubscriptionResource, SubscriptionResourceDoneable>> subscriptionResourceClient;

    ConcurrentHashMap<String, List<SubscriptionResource>> subscriptionResourceCache = new ConcurrentHashMap<>();

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
            return empty();
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
            return emptyList();
        }
        return unmodifiableList(subscriptionResourceCache.get(eventType));
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

}
