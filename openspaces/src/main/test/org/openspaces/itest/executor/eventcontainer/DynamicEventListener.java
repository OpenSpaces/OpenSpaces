package org.openspaces.itest.executor.eventcontainer;

import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;

/**
 * @author kimchy
 */
@EventDriven
@Polling(value = "test", gigaSpace = "gigaSpace1")
public class DynamicEventListener {

    private volatile boolean receivedEvent = false;

    @EventTemplate
    public Object template() {
        return new Object();
    }

    @SpaceDataEvent
    public void onEvent() {
        receivedEvent = true;
    }

    public boolean isReceivedEvent() {
        return receivedEvent;
    }

    public void setReceivedEvent(boolean receivedEvent) {
        this.receivedEvent = receivedEvent;
    }
}
