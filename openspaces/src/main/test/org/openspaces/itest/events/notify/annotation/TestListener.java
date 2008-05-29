package org.openspaces.itest.events.notify.annotation;

import org.openspaces.events.EventTemplate;
import org.openspaces.events.TransactionalEventContainer;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.notify.NotifyBatch;
import org.openspaces.events.notify.NotifyContainer;
import org.openspaces.events.notify.NotifyType;

/**
 * @author kimchy
 */
@NotifyContainer
@NotifyType(write = true)
@NotifyBatch(size = 1, time = 100)
@TransactionalEventContainer
public class TestListener {

    private volatile boolean receivedMessage = false;

    @EventTemplate
    public Object thisIsMyTemplate() {
        return new Object();
    }

    @SpaceDataEvent
    public void iAmTheListener(Object value) {
        receivedMessage = true;
    }

    public boolean isReceivedMessage() {
        return receivedMessage;
    }
}