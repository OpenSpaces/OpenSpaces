package org.openspaces.itest.events.notify.batch;

import org.openspaces.events.EventTemplate;
import org.openspaces.events.TransactionalEvent;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.notify.Notify;
import org.openspaces.events.notify.NotifyBatch;
import org.openspaces.events.notify.NotifyType;

/**
 * @author kimchy
 */
@Notify
@NotifyType(write = true)
@NotifyBatch(size = 2, time = 100000, passArrayAsIs = true)
@TransactionalEvent
public class TestListener {

    private volatile boolean receivedMessage = false;

    private volatile Object event;

    @EventTemplate
    public Object thisIsMyTemplate() {
        return new Object();
    }

    @SpaceDataEvent
    public void iAmTheListener(Object value) {
        receivedMessage = true;
        this.event = value;
    }

    public boolean isReceivedMessage() {
        return receivedMessage;
    }

    public Object getEvent() {
        return event;
    }
}