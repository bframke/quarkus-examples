package example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.quarkus.runtime.StartupEvent;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.quarkus.core.CamelMain;
import org.apache.camel.quarkus.core.CamelMainEvents;
import org.apache.camel.spi.Registry;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CamelContextListener {
    private static final Logger _logger = LoggerFactory.getLogger(CamelContextListener.class);

    static final String ENVIRONMENT_PREFIX = "env.";

    @ConfigProperty(name="quarkus.camel.routes-uris")
    String _contextFileLocations;
    @ConfigProperty(name="camel.activemq.connection.factories", defaultValue = "{}")
    String _activemqConfigsAsString;

    @Inject
    CamelMain _camelMain;
    @Inject
    ActiveMqConnectionFactoryProducer _activeMqConnectionFactoryProducer;

    private ObjectMapper _jsonMapper = new ObjectMapper();

    public void onInit(@Observes StartupEvent evt) throws Exception {
        _logger.info("config file read from: {}", _contextFileLocations);
        _logger.info("context : {}", _camelMain.getCamelContext());
    }

    public void configureCamel(@Observes CamelMainEvents.Configure evt) {
        setPropertiesFromEnvironment();
        registerActiveMqFactories();
    }

    public void camelStarted(@Observes CamelMainEvents.BeforeStart evt) throws Exception {
        /*RoutesBuilder builder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jms://queue://TO_REMOTE?disableReplyTo=true&acknowledgementModeName=CLIENT_ACKNOWLEDGE&connectionFactory=#amqConnectionFactoryPlain")
                        .id("bridge_TEST.AMQ.TO.IBM")
                        .to("jms://queue://FROM_REMOTE?disableReplyTo=true&acknowledgementModeName=CLIENT_ACKNOWLEDGE&connectionFactory=#ibmmqConnectionFactoryPlain");
                from("jms://queue://TO_REMOTE?disableReplyTo=true&acknowledgementModeName=CLIENT_ACKNOWLEDGE&connectionFactory=#ibmmqConnectionFactoryPlain")
                        .id("bridge_TEST.IBM.TO.AMQ")
                        .to("jms://queue://FROM_REMOTE?disableReplyTo=true&acknowledgementModeName=CLIENT_ACKNOWLEDGE&connectionFactory=#amqConnectionFactoryPlain");
            }
        };

        _camelMain.addRoutesBuilder(builder);*/
        _camelMain.configure().setXmlRoutes(_contextFileLocations);
    }





    void registerActiveMqFactories() {
        Map<String, JmsConfig> activemqConnectionFactoriesConfig = getJmsConfigs(_activemqConfigsAsString);
        _logger.info("activemqConnectionFactoriesConfig : {}", activemqConnectionFactoriesConfig);
        if (activemqConnectionFactoriesConfig != null) {
            Registry registry = _camelMain.getCamelContext().getRegistry();
            activemqConnectionFactoriesConfig.forEach((name, config) -> {
                registry.bind(name, _activeMqConnectionFactoryProducer.getConnectionFactory(config));
            });
        }
    }

    private void setPropertiesFromEnvironment() {
        System.getenv().forEach((key,value) -> System.setProperty(ENVIRONMENT_PREFIX +key, value));
    }

    private Map<String,JmsConfig> getJmsConfigs(String jsonValue) {
        try {
            return _jsonMapper.readValue(jsonValue,
                    TypeFactory.defaultInstance().constructMapLikeType(HashMap.class, String.class, JmsConfig.class));
        } catch (IOException e) {
            _logger.error("cannot convert config value '" + jsonValue +"'", e);
        }
        return null;
    }
}

