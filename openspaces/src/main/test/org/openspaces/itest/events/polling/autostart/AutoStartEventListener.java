package org.openspaces.itest.events.polling.autostart;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.SpaceDataEventListener;
import org.springframework.transaction.TransactionStatus;

public class AutoStartEventListener implements SpaceDataEventListener<Object> {

    private int messageCounter = 0;

    public void onEvent(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
        messageCounter++;
    }

    public int getMessageCounter() {
        return messageCounter;
    }

    @EventTemplate
    public Object getMySpecialTemplate() {
        return new Object();
    }

}
