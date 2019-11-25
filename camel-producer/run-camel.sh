#
# Use this if you don't want to use the parsed values in the application.properties
#
#export CAMEL_ACTIVEMQ_CONNECTION_FACTORIES="{
#  \"amqConnectionFactoryPlain\" : {
#    \"hostName\" : \"localhost\",
#    \"port\" : 61616,
#    \"ssl\" : false
#  },
#  \"amqConnectionFactory\" : {
#    \"hostName\" : \"localhost\",
#    \"port\" : 61617,
#    \"ssl\" : true,
#    \"trustStorePath\": \"${HOME}/camel/src/main/resources/truststore.jks\",
#    \"trustStorePassword\": \"test123\",
#    \"keyStorePath\": \"${HOME}/camel/src/main/resources/keystore.jks\",
#    \"keyStorePassword\": \"test123\"
#  }
#}"

# Native Run
#./build/camel-0.2.0-runner -Dquarkus.camel.routes-uris=file://$PWD/src/main/resources/bridge.xml

# Quarkus Build Run
$JAVA_HOME/bin/java  -Dquarkus.camel.routes-uris=file://$PWD/src/main/resources/bridge.xml -jar ./build/camel-0.2.0-runner.jar