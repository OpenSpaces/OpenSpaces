package org.openspaces.itest.events.notify.annotation;

import org.openspaces.events.EventTemplate;
import org.openspaces.events.TransactionalEventBean;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.notify.NotifyBatch;
import org.openspaces.events.notify.NotifyEventBean;
import org.openspaces.events.notify.NotifyType;

/**
 * @author kimchy
 */
@NotifyEventBean
@NotifyType(write = true)
@NotifyBatch(size = 1, time = 100)
@TransactionalEventBean
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