package org.openspaces.itest.events.polling.aspectlistener4;

import org.openspaces.events.adapter.SpaceDataEvent;

/**
 * @author kimchy
 */
public class TestListener {

    private volatile boolean receivedMessage = false;

    @SpaceDataEvent
    public void iAmTheListener(Object value) {
        receivedMessage = true;
    }

    public boolean isReceivedMessage() {
        return receivedMessage;
    }

    public void clearReceivedMessage() {
        this.receivedMessage = false;
    }
}