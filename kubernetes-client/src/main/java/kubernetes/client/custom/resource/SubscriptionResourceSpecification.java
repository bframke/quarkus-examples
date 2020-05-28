package kubernetes.client.custom.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import kubernetes.client.common.SubscriptionWithId;

import java.io.Serializable;
import java.util.Objects;

@JsonDeserialize
@RegisterForReflection
public class SubscriptionResourceSpecification implements Serializable {

    @JsonProperty("subscription")
    private SubscriptionWithId subscription;

    @JsonProperty("subscriberId")
    private String subscriberId;

    @JsonProperty("publisherId")
    private String publisherId;

    public SubscriptionResourceSpecification() {}

    public SubscriptionResourceSpecification(SubscriptionWithId subscription) {
        this.subscription = subscription;
    }

    public SubscriptionResourceSpecification(SubscriptionWithId subscription, String subscriberId, String publisherId) {
        this.subscription = subscription;
        this.subscriberId = subscriberId;
        this.publisherId = publisherId;
    }

    public SubscriptionWithId getSubscription() {
        return subscription;
    }

    public SubscriptionResourceSpecification setSubscription(SubscriptionWithId subscription) {
        this.subscription = subscription;
        return this;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public SubscriptionResourceSpecification setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
        return this;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public SubscriptionResourceSpecification setPublisherId(String publisherId) {
        this.publisherId = publisherId;
        return this;
    }

    @Override
    public String toString() {
        return "SubscriptionResourceSpecification{" +
                "subscription=" + subscription +
                ", subscriberId='" + subscriberId + '\'' +
                ", publisherId='" + publisherId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionResourceSpecification)) return false;
        SubscriptionResourceSpecification that = (SubscriptionResourceSpecification) o;
        return subscription.equals(that.subscription) &&
                subscriberId.equals(that.subscriberId) &&
                Objects.equals(publisherId, that.publisherId);
    }

    public boolean equalsWithoutId(SubscriptionResourceSpecification other) {
        if (this == other) return true;
        if (other == null) return false;
        return subscription.equalsSubscriptionWithoutId(other.subscription) &&
                subscriberId.equals(other.subscriberId) &&
                Objects.equals(publisherId, other.publisherId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscription, subscriberId, publisherId);
    }

}
