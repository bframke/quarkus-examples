package de.code.client.common.model;

import com.google.gson.annotations.SerializedName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

import static de.code.client.common.model.Constants.PATTERN_TYPE;

@JsonDeserialize
public class Subscription {
    public static final String DELIVERYTYPE_CALLBACK = "callback";

    @SerializedName("trigger")
    @Valid
    @JsonProperty("trigger")
    private Trigger trigger = null;

    @Size(min = 1, max = 100)
    @NotNull
    @Pattern(message="Type must contain only of a-z, A-Z, 0-9, '.', '-'", regexp = PATTERN_TYPE)
    @JsonProperty("type")
    private String type;

    @Pattern (message="must be a valid URL", regexp = Constants.PATTERN_URL)
    @Size(min = 1)
    @JsonProperty("callback")
    private String callback;

    @NotNull
    @Pattern(regexp = "(data|dataref)")
    @JsonProperty("payloadType")
    private String payloadType;

    @NotNull
    @Pattern(regexp = "(server_sent_event|callback)")
    @JsonProperty("deliveryType")
    private String deliveryType;

    public Subscription() {
        type = "";
        payloadType = "";
        deliveryType = "";
    }

    protected Subscription(Subscription subscription) {
        callback = subscription.callback;
        trigger = subscription.trigger;
        deliveryType = subscription.deliveryType;
        payloadType = subscription.payloadType;
        type = subscription.type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public Subscription trigger(Trigger trigger) {
        this.trigger = trigger;
        return this;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof Subscription)) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(this.trigger, that.trigger) &&
                type.equals(that.type) &&
                Objects.equals(callback, that.callback) &&
                payloadType.equals(that.payloadType) &&
                deliveryType.equals(that.deliveryType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trigger);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Subscription {\n");

        sb.append("    trigger: ").append(toIndentedString(trigger)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
