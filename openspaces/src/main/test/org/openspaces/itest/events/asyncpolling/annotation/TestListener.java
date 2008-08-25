package org.openspaces.itest.events.asyncpolling.annotation;

import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.asyncpolling.AsyncHandler;
import org.openspaces.events.asyncpolling.AsyncPolling;
import org.openspaces.events.asyncpolling.receive.AsyncOperationHandler;
import org.openspaces.events.asyncpolling.receive.SingleTakeAsyncOperationHandler;
import org.openspaces.itest.core.simple.Message;

/**
 * @author kimchy
 */
@AsyncPolling
// TODO disable this for now as there is a bug in asycn task with transaciton in the core
//@TransactionalEvent
public class TestListener {

    private volatile boolean receivedMessage = false;

    @AsyncHandler
    public AsyncOperationHandler thisIsMyReceiveOperationHandler() {
        return new SingleTakeAsyncOperationHandler();
    }

    @EventTemplate
    public Message thisIsMyTemplate() {
        return new Message();
    }

    @SpaceDataEvent
    public void iAmTheListener(Object value) {
        receivedMessage = true;
    }

    public boolean isReceivedMessage() {
        return receivedMessage;
    }

    public void setReceivedMessage(boolean receivedMessage) {
        this.receivedMessage = receivedMessage;
    }
}