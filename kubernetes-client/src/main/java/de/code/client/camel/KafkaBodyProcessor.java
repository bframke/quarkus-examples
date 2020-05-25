package de.code.client.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.code.client.common.model.Event;
import de.code.client.kafka.MessageEnvelope;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class KafkaBodyProcessor implements Processor {
    private static final Logger _logger = LoggerFactory.getLogger(KafkaBodyProcessor.class);

    @Inject
    ObjectMapper objectMapper;

    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getMessage();
        String rawPayload = message.getBody().toString();
        _logger.debug("raw payload: {}", rawPayload);

        MessageEnvelope complexPayload = MessageEnvelope.fromJson(rawPayload);
        _logger.info("headers: {}", complexPayload.getHeaders());

        message.setBody(objectMapper.readValue(complexPayload.getPayloadAsString(), Event.class));
        message.setHeaders(sanitizeHeaders(complexPayload.getHeaders()));
    }

    private Map<String, Object> sanitizeHeaders(Map<String, Object> headers) {
        headers.entrySet().
                forEach(entry -> {
                    Object value = entry.getValue();
                    if (HttpHeaderNames.CONTENT_TYPE.contentEqualsIgnoreCase(entry.getKey()) && entry.getValue() instanceof List) {
                        entry.setValue(((List<String>)value).get(0));
                    }
                });
        return headers;
    }

}
