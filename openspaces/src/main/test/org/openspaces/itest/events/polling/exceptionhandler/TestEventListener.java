package org.openspaces.itest.events.polling.exceptionhandler;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.SpaceDataEventListener;
import org.springframework.transaction.TransactionStatus;

public class TestEventListener implements SpaceDataEventListener<Object> {

    private volatile int messageCounter = 0;

    private volatile int throwExceptionTillCounter = -1;

    public void reset() {
        this.messageCounter = 0;
        this.throwExceptionTillCounter = -1;
    }

    public void setThrowExceptionTillCounter(int throwExceptionTillCounter) {
        this.throwExceptionTillCounter = throwExceptionTillCounter;
    }

    public void onEvent(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
        messageCounter++;
        if (messageCounter < throwExceptionTillCounter) {
            throw new RuntimeException("FAIL");
        }
    }

    public int getMessageCounter() {
        return messageCounter;
    }

    @EventTemplate
    public Object getMySpecialTemplate() {
        return new Object();
    }

}