package kubernetes.client;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;

import kubernetes.client.custom.resource.SubscriptionResource;
import kubernetes.client.custom.resource.SubscriptionResourceDoneable;
import kubernetes.client.custom.resource.SubscriptionResourceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import static java.lang.String.format;

public class ClientProvider {

    private static final Logger logger = LoggerFactory.getLogger(ClientProvider.class);

    private static final String CUSTOM_API_VERSION = "subscriber/v1";
    private static final String CUSTOM_KIND = "Subscription";
    private static final String CUSTOM_DEFINITION_NAME = "subscriptions.subscriber";

    @Produces
    @Singleton
    NonNamespaceOperation<SubscriptionResource, SubscriptionResourceList, SubscriptionResourceDoneable, Resource<SubscriptionResource, SubscriptionResourceDoneable>>
    makeCustomResourceClient(KubernetesClient defaultClient) {
        logger.info("Current used Namespace: {}", defaultClient.getNamespace());
        KubernetesDeserializer.registerCustomKind(CUSTOM_API_VERSION, CUSTOM_KIND, SubscriptionResource.class);

        CustomResourceDefinition crd = defaultClient
                .customResourceDefinitions()
                .list()
                .getItems()
                .stream()
                .filter(definition -> CUSTOM_DEFINITION_NAME.equals(definition.getMetadata().getName()))
                .findAny().orElseThrow(
                        () -> new RuntimeException(format("Deployment error: Custom Resource Definition %1s not found.", CUSTOM_DEFINITION_NAME)));

        return defaultClient
                .customResources(crd, SubscriptionResource.class, SubscriptionResourceList.class, SubscriptionResourceDoneable.class)
                .inNamespace(defaultClient.getNamespace());
    }
}
