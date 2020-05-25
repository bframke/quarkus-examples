package de.code.client.camel;

import de.code.client.common.model.Event;
import de.code.client.common.model.Subscription;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TriggerFilter {

    public boolean applies(Subscription subscription, Event event) {
        subscription.getTrigger();
        event.getData();
        return true;
    }
}
