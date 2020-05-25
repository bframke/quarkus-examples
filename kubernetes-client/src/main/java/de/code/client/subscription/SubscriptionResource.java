package de.code.client.subscription;

import de.code.client.common.model.*;
import de.code.client.kubernetes.cache.SubscriptionCache;
import de.code.client.kubernetes.handler.SubscriptionHandler;
import de.code.client.kubernetes.resources.SubscriptionResourceSpecification;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static de.code.client.common.model.Subscription.DELIVERYTYPE_CALLBACK;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@ApplicationScoped
@Path("/v1")
public class SubscriptionResource {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionResource.class);
    public static final String X_SUBSCRIPTION_ID = "X-Subscription-Id";
    public static final String X_SUBSCRIBER_ID = "X-Pubsub-Subscriber-Id";
    public static final String PARAM_SUBSCRIBER_ID = "subscriberId";
    public static final String APPLICATION_STREAM_JSON = "application/stream+json";

    @Inject
    SubscriptionCache subscriptionCache;

    @Inject
    SubscriptionHandler subscriptionHandler;

    @Inject
    ProblemFactory problemFactory;

    @Inject
    Validator validator;

    @Path("subscriptions")
    @GET
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getSubscriptions(@QueryParam(PARAM_SUBSCRIBER_ID) String subscriberId) {
        logger.info("list all subscriptions for subscriberId '{}'", subscriberId);
        return Response.ok().entity(subscriptionCache.getAllSubscriptions(subscriberId)).build();
    }

    @Path("subscriptions")
    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Timed
    public Response createSubscription(@NotNull(message = "subscription must not be null")
                                       @Parameter(name = "subscription", required = true)
                                       Subscription subscription,
                                       @HeaderParam(X_SUBSCRIBER_ID)
                                       @Parameter(description = "Consumer subject from consumer's access token.", required = true)
                                       String subscriberId) {
        logger.info("create subscription request for subscriberId '{}'", subscriberId);
        SubscriptionResourceSpecification subscriptionResourceSpecification =
                new SubscriptionResourceSpecification(new SubscriptionWithId(subscription).setSubscriptionId(UUID.randomUUID().toString()))
                    .setSubscriberId(subscriberId);

        return validateSubscription(subscriptionResourceSpecification).orElseGet(() ->
                subscriptionHandler.createSubscriptionResource(subscriptionResourceSpecification)
                    .map(problem -> Response.status(problem.getStatus()).entity(problem).build())
                    .orElseGet(() -> createResponse(subscriptionResourceSpecification.getSubscription())));
    }

    private Response createResponse(SubscriptionWithId subscriptionWithId) {
        return DELIVERYTYPE_CALLBACK.equals(subscriptionWithId.getDeliveryType())
                ? Response.status(CREATED)
                    .header(X_SUBSCRIPTION_ID, subscriptionWithId.getSubscriptionId()).build()
                : Response.status(SEE_OTHER)
                    .header(X_SUBSCRIPTION_ID, subscriptionWithId.getSubscriptionId())
                    .header(LOCATION, buildServerSentEventsUrl(subscriptionWithId.getSubscriptionId()))
                    .build();
    }

    @Path("events/{subscriptionId}")
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_STREAM_JSON})
    @Counted
    public Response getServerSentEvents(@Parameter(name = "subscriptionId", description = "Id of the subscription") @PathParam("subscriptionId") String subscriptionId) {
        return Response.status(SERVICE_UNAVAILABLE)
                .entity(problemFactory.createProblem().status(SERVICE_UNAVAILABLE.getStatusCode())
                .title("not yet implemented")).build();
    }

    @Path("subscriptions/{subscriptionId}")
    @DELETE
    @Produces(APPLICATION_JSON)
    @Timed
    public Response deleteSubscription(@NotNull(message = "subscriptionId must not be null")
                                       @Parameter(name = "subscriptionId", required = true)
                                       @PathParam( "subscriptionId") String subscriptionId) {
        logger.info("delete request for subscription {}", subscriptionId);
        Holder<Boolean> success = new Holder<>(false);
        return validateUuid(subscriptionId).orElseGet(() ->
            subscriptionHandler.deleteSubscriptionResource(subscriptionId, success)
                .map(problem -> Response.status(problem.getStatus()).entity(problem).build())
                .orElseGet(() -> success.get() ?
                        Response.noContent().build() :
                        Response.status(NOT_FOUND)
                                .entity(problemFactory.createProblem()
                                        .status(NOT_FOUND.getStatusCode())
                                        .title(String.format("no such subscription: %s", subscriptionId)))
                                .build()));
    }

    Optional<Response> validateUuid(String subscriptionId) {
        try {
            UUID.fromString(subscriptionId);
            return empty();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return of(Response.status(BAD_REQUEST)
                    .entity(problemFactory.createProblem().status(BAD_REQUEST.getStatusCode()).title(ex.getMessage())).build());
        }
    }

    Optional<Response> validateSubscription(SubscriptionResourceSpecification subscriptionResourceSpecification) {
        String subscriberId = subscriptionResourceSpecification.getSubscriberId();
        Set<ConstraintViolation<Subscription>> violations = validator.validate(subscriptionResourceSpecification.getSubscription());

        if (violations.isEmpty() && ! isEmpty(subscriberId)) {
            return empty();
        }

        String aggregatedMessage = buildAggregateMessage(violations, subscriberId);
        logger.error("subscription is invalid");
        return of(Response.status(BAD_REQUEST)
                .entity(problemFactory.createProblem().status(BAD_REQUEST.getStatusCode())
                        .title("subscription is invalid").detail(aggregatedMessage)).build());
    }

    private String buildAggregateMessage(Set<ConstraintViolation<Subscription>> violations, String subscriberId) {
        String aggregateMessage = violations.stream()
                .map(violation -> format("%1s: %2s", violation.getPropertyPath(), violation.getMessage()))
                .collect(joining("\n"));
        if (isEmpty(subscriberId)) {
            aggregateMessage += "\n" + X_SUBSCRIBER_ID + " must not be null";
        }
        return aggregateMessage;
    }

    private String buildServerSentEventsUrl(String uuid) {
        return format("/v1/events/%s", uuid);
    }

}
