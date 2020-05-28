package kubernetes.client.custom.resource;

import io.fabric8.kubernetes.client.CustomResource;

public class SubscriptionResource extends CustomResource {

    public SubscriptionResource() {
        super("Subscription");
    }

    private SubscriptionResourceSpecification specification;

    public SubscriptionResourceSpecification getSpec() {
        return specification;
    }

    public void setSpec(SubscriptionResourceSpecification specification) {
        this.specification = specification;
    }

    @Override
    public String toString() {
        String name = getMetadata() != null ? getMetadata().getName() : "unknown";
        String version = getMetadata() != null ? getMetadata().getResourceVersion() : "unknown";
        return "name=" + name + " version=" + version + " value=" + specification;
    }

}
