package camel.kafka.sasl.producing;

import camel.kafka.sasl.objects.MessageEnvelope;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.jboss.resteasy.util.HttpHeaderNames.CONNECTION;

public class CKSProcessor implements Processor {

    private static final List<String> SUPPRESSED_HEADERS = Arrays.asList(AUTHORIZATION.toLowerCase(), CONNECTION.toLowerCase());

    @Override
    public void process(Exchange exchange) {
        Message message = exchange.getMessage();
        Map<String, Object> headers = message.getHeaders();
        Map<String, Object> innerHeaders = new HashMap<>();

        String event = message.getBody(String.class);

        filterHeaders(headers, innerHeaders);

        headers.put(KafkaConstants.PARTITION_KEY, "0");
        headers.put(KafkaConstants.KEY, "http://apihost/some/path/resource/1234");
        headers.put(KafkaConstants.OVERRIDE_TOPIC, "cks.test.topic");

        message.setBody(new MessageEnvelope(innerHeaders, event));
    }

    void filterHeaders(Map<String,Object> inHeaders, Map<String,Object> headers) {
        inHeaders.entrySet().stream()
                .filter(entry -> ! entry.getKey().startsWith("kafka."))
                .filter(entry -> ! SUPPRESSED_HEADERS.contains(entry.getKey().toLowerCase()))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .forEach(headers::put);
    }
}
