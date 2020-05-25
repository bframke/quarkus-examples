package de.code.client.common.model;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class ProblemFactory {

    @ConfigProperty(name="hostname", defaultValue = "unknown")
    Optional<String> instanceId;

    public Problem createProblem() {
        return new Problem().instance(instanceId.orElse("unknown"));
    }

}
