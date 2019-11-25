package example;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import static org.apache.commons.lang3.StringUtils.*;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;

@ApplicationScoped
public class ActiveMqConnectionFactoryProducer implements ConnectionFactoryProducer {

    private static final String PLAIN_CONNECTION_URL_FORMAT = "tcp://%s:%d";
    private static final String SSL_CONNECTION_URL_FORMAT = "tcp://%1$s:%2$d?sslEnabled=true&trustStorePath=%3$s&trustStorePassword=%4$s&keyStorePath=%5$s&keyStorePassword=%6$s";

    @Override
    public ConnectionFactory getConnectionFactory(JmsConfig config) {
        String connectionUrl;
        if (! config.isSsl()) {
            connectionUrl = String.format(PLAIN_CONNECTION_URL_FORMAT,
                    config.getHostName(),
                    config.getPort()
            );
        } else {
            connectionUrl = String.format(SSL_CONNECTION_URL_FORMAT,
                    config.getHostName(),
                    config.getPort(),
                    config.getTrustStorePath(),
                    config.getTrustStorePassword(),
                    config.getKeyStorePath(),
                    config.getKeyStorePassword()
            );
        }
        ActiveMQConnectionFactory factory = isEmpty(config.getUserName()) ?
                new ActiveMQConnectionFactory(connectionUrl) :
                new ActiveMQConnectionFactory(connectionUrl, config.getUserName(), config.getPassword());
        return factory;
    }

}

