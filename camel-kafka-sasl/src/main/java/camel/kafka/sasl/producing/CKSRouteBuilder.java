package camel.kafka.sasl.producing;

import camel.kafka.sasl.config.Config;
import camel.kafka.sasl.objects.KafkaUrlTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static java.lang.String.format;

@ApplicationScoped
public class CKSRouteBuilder extends RouteBuilder {

    @Inject
    Config config;

    @Inject
    KafkaUrlTemplate kafkaUrlTemplate;

    @Override
    public void configure() {
        onException(UnknownTopicOrPartitionException.class)
                .handled(true)
                .maximumRedeliveries(1)
                .redeliveryDelay(100)
                .handled(false);

        from("direct:kafka")
                .id("toKafka")
                .process(new CKSProcessor())
                .to(format(kafkaUrlTemplate.getUrlTemplate(), "topic", config.getBrokerList()));
    }
}
