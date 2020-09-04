package camel.kafka.sasl.objects;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class KafkaUrlTemplate {

    @ConfigProperty(name = "camel.truststore.location", defaultValue = "")
    Optional<String> trustStoreLocation;

    @ConfigProperty(name = "camel.truststore.password", defaultValue = "")
    Optional<String> trustStorePassword;

    @ConfigProperty(name = "kafka.security-protocol", defaultValue = "")
    Optional<String> securityProtocol;

    String urlTemplate = "kafka:%s?brokers=%s&groupId=cks";

    public String getUrlTemplate() {
        trustStoreLocation.ifPresent(location -> urlTemplate += "&sslTruststoreLocation=" + location);
        trustStorePassword.ifPresent(password -> urlTemplate += "&sslTruststorePassword=" + password);
        securityProtocol.ifPresent(protocol -> urlTemplate += "&securityProtocol=" + protocol);
        return urlTemplate;
    }
}
