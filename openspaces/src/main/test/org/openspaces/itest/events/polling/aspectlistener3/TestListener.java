package org.openspaces.itest.events.polling.aspectlistener3;

/**
 * @author kimchy
 */
public interface TestListener {

    void onEvent(Object value);

    boolean isReceivedMessage();

    void clearReceivedMessage();
}