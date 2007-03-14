package org.openspaces.events.polling.trigger;

import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

/**
 * A trigger operation handler that performa read based on the provided template and returns its
 * result.
 * 
 * @author kimchy
 */
public class ReadTriggerOperationHandler implements TriggerOperationHandler {

    private boolean useTriggerAsTemplate = false;

    /**
     * Controls if the object returned from
     * {@link #triggerReceive(Object,org.openspaces.core.GigaSpace,long)} will be used as the
     * template for the receive operation by returnning <code>true</code>. If <code>false</code>
     * is returned, the actual template configured in the polling event container will be used.
     * 
     * @see TriggerOperationHandler#isUseTriggerAsTemplate()
     */
    public void setUseTriggerAsTemplate(boolean useTriggerAsTemplate) {
        this.useTriggerAsTemplate = useTriggerAsTemplate;
    }

    /**
     * @see TriggerOperationHandler#isUseTriggerAsTemplate()
     */
    public boolean isUseTriggerAsTemplate() {
        return this.useTriggerAsTemplate;
    }

    /**
     * Uses {@link org.openspaces.core.GigaSpace#read(Object,long)} and returns its result.
     */
    public Object triggerReceive(Object template, GigaSpace gigaSpace, long receiveTimeout) throws DataAccessException {
        return gigaSpace.read(template, receiveTimeout);
    }

}
