package kubernetes.client.custom.resource;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class SubscriptionResourceDoneable extends CustomResourceDoneable<SubscriptionResource> {

    public SubscriptionResourceDoneable(SubscriptionResource resource, Function<SubscriptionResource, SubscriptionResource> function) {
        super(resource, function);
    }
}
