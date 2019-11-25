FROM ubuntu:18.04
COPY camel-producer-0.2.0-runner /application
COPY bridge.xml libsunec.so keystore.jks truststore.jks /
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0", "-Dquarkus.camel.routes-uris=file:///bridge.xml"]
