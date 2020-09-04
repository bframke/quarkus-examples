package camel.kafka.sasl.producing;

import org.apache.camel.ProducerTemplate;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.TreeMap;

import static javax.ws.rs.core.Response.Status.CREATED;

@ApplicationScoped
@Path("/")
public class Endpoint {

    private static final Logger logger = LoggerFactory.getLogger(Endpoint.class);

    @Inject
    ProducerTemplate producerTemplate;

    @POST
    @Path("events")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendEvent(@NotNull(message = "event must not be null") String event,
                              @Context HttpHeaders httpHeaders) {
        producerTemplate.sendBodyAndHeaders("direct:kafka", event, convertHeaders(httpHeaders.getRequestHeaders()));
        return Response.status(CREATED).build();
    }

    private Map<String, Object> convertHeaders(MultivaluedMap<String, String> requestHeaders) {
        Map<String, Object> allHeaders = new TreeMap<>(String::compareToIgnoreCase);
        requestHeaders.entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> ! entry.getValue().isEmpty())
                .forEach(entry -> allHeaders.put(entry.getKey(), entry.getValue().get(0)));
        allHeaders.putAll(MDC.getMap());
        return allHeaders;
    }
}
