package de.code.client.kubernetes.handler;

import de.code.client.common.model.Holder;
import de.code.client.common.model.Problem;
import de.code.client.common.model.ProblemFactory;
import de.code.client.kubernetes.cache.SubscriptionCache;
import de.code.client.kubernetes.resources.SubscriptionResource;
import de.code.client.kubernetes.resources.SubscriptionResourceDoneable;
import de.code.client.kubernetes.resources.SubscriptionResourceList;
import de.code.client.kubernetes.resources.SubscriptionResourceSpecification;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

import static java.util.Optional.*;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@ApplicationScoped
public class SubscriptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionHandler.class);
    private static final String SUBSCRIPTION_NOT_FOUND = "Could not delete Subscription. Subscription not found";

    @Inject
    SubscriptionCache subscriptionCache;
    @Inject
    ProblemFactory problemFactory;

    @Inject
    NonNamespaceOperation<SubscriptionResource,
            SubscriptionResourceList,
            SubscriptionResourceDoneable,
            Resource<SubscriptionResource, SubscriptionResourceDoneable>> subscriptionResourceClient;

    public Optional<Problem> createSubscriptionResource(SubscriptionResourceSpecification subscriptionResourceSpecification) {
        SubscriptionResource subscriptionResource = new SubscriptionResource();
        subscriptionResource.setSpec(subscriptionResourceSpecification);
        String subscriptionId = subscriptionResourceSpecification.getSubscription().getSubscriptionId();
        return ofNullable(subscriptionCache.addSubscription(subscriptionResource)
                .orElseGet(() -> {
                    if (subscriptionId.equals(subscriptionResourceSpecification.getSubscription().getSubscriptionId())) {
                        logger.info("writing new subscription as custom resource");
                        ObjectMeta metadata = subscriptionResource.getMetadata();
                        metadata.setName(subscriptionResourceSpecification.getSubscription().getSubscriptionId());
                        try {
                            subscriptionResourceClient.create(subscriptionResource);
                        } catch (Exception e) {
                            logger.error("could not write subscription", e);
                            return problemFactory.createProblem().title(e.getMessage())
                                    .status(INTERNAL_SERVER_ERROR.getStatusCode());
                        }
                    } else {
                        logger.warn("found duplicate subscription, will not write resource");
                    }
                    return null;
                }));

    }

    public Optional<Problem> deleteSubscriptionResource(String subscriptionId, Holder<Boolean> success) {
        try {
            Optional<SubscriptionResource> foundSubscription = subscriptionCache.getSubscriptionForId(subscriptionId);
            if ( ! foundSubscription.isPresent()) {
                return of(problemFactory.createProblem().status(NOT_FOUND.getStatusCode()).title(SUBSCRIPTION_NOT_FOUND));
            }
            Optional<Problem> problem = subscriptionCache.deleteSubscription(subscriptionId, success);
            if (problem.isPresent() || ! success.get()) {
                return problem;
            }
            success.set(subscriptionResourceClient.delete(foundSubscription.get()));
            return success.get() ? empty() : of(problemFactory.createProblem().status(NOT_FOUND.getStatusCode()).title(SUBSCRIPTION_NOT_FOUND));
        } catch (Exception e) {
            logger.error("could not delete resource", e);
            return of(problemFactory.createProblem()
                .status(INTERNAL_SERVER_ERROR.getStatusCode()).title(e.getMessage()));
        }
    }
}
