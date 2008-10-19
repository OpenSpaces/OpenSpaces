package org.openspaces.itest.events.polling.aspectlistener3;

import org.openspaces.events.adapter.SpaceDataEvent;

/**
 * @author kimchy
 */
public class DefaultTestListener implements TestListener {

    private volatile boolean receivedMessage = false;

    @SpaceDataEvent
    public void onEvent(Object value) {
        receivedMessage = true;
    }

    public boolean isReceivedMessage() {
        return receivedMessage;
    }

    public void clearReceivedMessage() {
        this.receivedMessage = false;
    }
}