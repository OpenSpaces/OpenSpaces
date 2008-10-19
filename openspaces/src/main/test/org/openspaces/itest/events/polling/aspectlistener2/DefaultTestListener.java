package org.openspaces.itest.events.polling.aspectlistener2;

/**
 * @author kimchy
 */
public class DefaultTestListener implements TestListener {

    private volatile boolean receivedMessage = false;

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