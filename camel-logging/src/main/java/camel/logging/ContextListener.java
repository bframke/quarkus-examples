package camel.logging;

import io.quarkus.runtime.StartupEvent;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.quarkus.core.CamelMain;
import org.apache.camel.quarkus.core.CamelMainEvents;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ContextListener {
    private static final Logger _logger = LoggerFactory.getLogger(ContextListener.class);
    private static final String CORRELATION_ID = "X-Correlation-Id";

    @Inject
    CamelMain _camelMain;
    private CamelContext _currentContext;

    @ConfigProperty(name = "alerte.kafka.broker.list", defaultValue = "localhost:9092")
    String _brokerList;
    @ConfigProperty(name = "alerte.kafka.listen.topics", defaultValue = "my-topic")
    String _topicList;

    public void onInit(@Observes StartupEvent evt) {
        _currentContext = _camelMain.getCamelContext();
        _logger.info("context : {}", _currentContext);
    }

    public void camelStarted(@Observes CamelMainEvents.BeforeStart evt) throws Exception {
        if(_currentContext == null) {
            _currentContext = _camelMain.getCamelContext();
        }

        RoutesBuilder builder = new RouteBuilder() {
            @Override
            public void configure() {
                restConfiguration()
                        .component("netty-http")
                        .host("0.0.0.0")
                        .port("8081")
                        .bindingMode(RestBindingMode.auto);
                rest("/")
                        .post("/topic/{topicId}/event")
                        .consumes("application/json")
                        .to("direct:kafka");
                from("direct:kafka")
                        .id("toKafka")
                        //.log("${body}") Comment in to get stuck
                        .process(exchange -> {
                            Map<String, Object> headers = exchange.getMessage().getHeaders();
                            Map<String, Object> innerHeaders = new HashMap<>();

                            filterKafkaHeaders(headers, innerHeaders);
                            createStandardHeadersIfNeeded(innerHeaders);

                            headers.put(KafkaConstants.PARTITION_KEY, "0");
                            headers.put(KafkaConstants.KEY, "1");
                            headers.put(KafkaConstants.OVERRIDE_TOPIC, exchange.getMessage().getHeader("topicId").toString());

                            // Comment in to get stuck
                            /*_logger.info("Headers for Kafka: {}", headers);
                            _logger.info("Headers for Payload: {}", innerHeaders);
                            _logger.info("Payload: {}", exchange.getMessage().getBody());*/

                            exchange.getMessage().setBody(new MessageEnvelope(innerHeaders, exchange.getMessage().getBody()));
                        })
                        .to("kafka:topic?brokers="+_brokerList);
                from("direct:log").log("${body}"); // use to get stuck
            }
        };

        _currentContext.addRoutes(builder);
        Thread.sleep(200);
    }



    private void createStandardHeadersIfNeeded(Map<String, Object> headers) {
        if (! headers.containsKey(CORRELATION_ID)) {
            headers.put(CORRELATION_ID, UUID.randomUUID());
        }
        if (! headers.containsKey(Exchange.CORRELATION_ID)) {
            headers.put(Exchange.CORRELATION_ID, headers.get(CORRELATION_ID));
        }
    }

    private void filterKafkaHeaders(Map<String,Object> inHeaders, Map<String,Object> headers) {
        inHeaders.remove( "CamelHttpPath" );
        inHeaders.entrySet().stream()
                .filter(entry -> ! entry.getKey().startsWith("kafka."))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .forEach(headers::put);
    }
}
