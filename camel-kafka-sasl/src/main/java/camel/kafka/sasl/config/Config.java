package camel.kafka.sasl.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class Config {

    @ConfigProperty(name = "cks.kafka.broker.list", defaultValue = "localhost:9092")
    Optional<String> brokerList;

    public String getBrokerList() {
        return brokerList.orElse("");
    }

}
