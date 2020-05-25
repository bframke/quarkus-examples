package de.code.client.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize
public class SubscriptionWithId extends Subscription implements Serializable {

    public SubscriptionWithId() {
        super();
    }

    public SubscriptionWithId(Subscription subscription) {
        super(subscription);
    }

    @JsonProperty("subscriptionId")
    private String subscriptionId;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public SubscriptionWithId setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public boolean equalsSubscriptionWithoutId(SubscriptionWithId subscription) {
        return super.equals(subscription);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionWithId)) return false;
        if (!super.equals(o)) return false;
        SubscriptionWithId that = (SubscriptionWithId) o;
        return Objects.equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public String toString() {
        return "SubscriptionResult{" +
                "subscriptionId='" + subscriptionId + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subscriptionId);
    }
}
