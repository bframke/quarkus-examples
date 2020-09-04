package camel.kafka.sasl.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.Map;

public class MessageEnvelope {

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    @JsonProperty
    private Map<String, Object> headers;
    @JsonProperty
    private Object payload;

    public MessageEnvelope() {
        jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public MessageEnvelope(Map<String, Object> headers, Object payload) {
        this();

        this.headers = headers;
        this.payload = payload;
    }

    @Override
    public String toString() {
        try {
            return jsonMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

    public static MessageEnvelope fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, MessageEnvelope.class);
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Object getPayload() {
        return payload;
    }

    @JsonIgnore
    public String getPayloadAsString() throws JsonProcessingException {
        if (payload instanceof String) {
            return (String)payload;
        } else {
            return jsonMapper.writeValueAsString(payload);
        }
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

}
