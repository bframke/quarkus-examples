package example;

import javax.jms.ConnectionFactory;

public interface ConnectionFactoryProducer {
    ConnectionFactory getConnectionFactory(JmsConfig jmsConfig);
}
