package org.openspaces.itest.events.polling.autostart;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventTemplateProvider;
import org.openspaces.events.SpaceDataEventListener;
import org.springframework.transaction.TransactionStatus;

public class AutoStartEventListener implements SpaceDataEventListener<Object>, EventTemplateProvider {

    private int messageCounter = 0;

    public void onEvent(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
        messageCounter++;
    }

    public int getMessageCounter() {
        return messageCounter;
    }

    public Object getTemplate() {
        return new Object();
    }

}
