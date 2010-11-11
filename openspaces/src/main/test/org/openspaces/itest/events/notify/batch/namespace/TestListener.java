package org.openspaces.itest.events.notify.batch.namespace;

import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;

/**
 * Uses xml to define the following:
 * // @Notify
 * // @NotifyType(write = true)
 * // @NotifyBatch(size = 2, time = 100000, passArrayAsIs = true)
 * // @TransactionalEvent
 * 
 * @see org.openspaces.itest.events.notify.batch.NotifyContainerBatchTests
 * @author Moran Avigdor
 */
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