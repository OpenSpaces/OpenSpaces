package org.openspaces.itest.events.polling.annotation;

import org.openspaces.events.EventTemplate;
import org.openspaces.events.TransactionalEventBean;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.PollingEventBean;
import org.openspaces.events.polling.ReceiveHandler;
import org.openspaces.events.polling.receive.MultiTakeReceiveOperationHandler;
import org.openspaces.events.polling.receive.ReceiveOperationHandler;

/**
 * @author kimchy
 */
@PollingEventBean
@TransactionalEventBean
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
