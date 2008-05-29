package org.openspaces.itest.events.polling.annotation;

import org.openspaces.events.EventTemplate;
import org.openspaces.events.TransactionalEventContainer;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.PollingContainer;
import org.openspaces.events.polling.ReceiveHandler;
import org.openspaces.events.polling.receive.MultiTakeReceiveOperationHandler;
import org.openspaces.events.polling.receive.ReceiveOperationHandler;

/**
 * @author kimchy
 */
@PollingContainer
@TransactionalEventContainer
public class TestListener {

    private volatile boolean receivedMessage = false;

    @ReceiveHandler
    public ReceiveOperationHandler thisIsMyReceiveOperationHandler() {
        return new MultiTakeReceiveOperationHandler();
    }

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
