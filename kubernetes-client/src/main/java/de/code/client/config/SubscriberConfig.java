package de.code.client.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class SubscriberConfig {
    @ConfigProperty(name = "horizon.kafka.broker.list")
    Optional<String> kafkaBrokers;

    public String getKafkaBrokers() {
        return kafkaBrokers.orElse("kafka brokers unknown");
    }

    public void setKafkaBrokers(String kafkaBrokers) {
        this.kafkaBrokers = Optional.of(kafkaBrokers);
    }

}
