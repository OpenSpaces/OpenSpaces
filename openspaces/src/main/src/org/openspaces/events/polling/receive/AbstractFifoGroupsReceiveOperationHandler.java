package org.openspaces.events.polling.receive;

import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.core.client.TakeModifiers;

/**
 * Support class to perform receive operations with or without Fifo Group.
 * <p>If configured to use Fifo Groups, the read/take operation will use {@link ReadModifiers#FIFO_GROUPS_POLL} / {@link TakeModifiers#FIFO_GROUPS_POLL} accordingly.
 * <ul><b>Note:</b> 
 * <li>All the handlers that uses the Fifo Groups capability should be used with a template that uses Fifo Groups </li>
 * <li>All the handlers that uses the Fifo Groups capability must be performed under a transaction </li>
 * </ul>
 * @author yael
 * @since 9.0
 */
public abstract class AbstractFifoGroupsReceiveOperationHandler extends AbstractNonBlockingReceiveOperationHandler {
    
    protected boolean fifoGroups = false;

    
    public boolean isFifoGroups() {
        return fifoGroups;
    }

    /**
     * Allows to configure the take/read operation to be performed in a Fifo Groups manner.
     * 
     * @param fifoGroups- if true, will use {@link ReadModifiers#FIFO_GROUPS_POLL} / {@link TakeModifiers#FIFO_GROUPS_POLL} as read/take modifiers.
     */
    public void setFifoGroups(boolean fifoGroups) {
        this.fifoGroups = fifoGroups;
    }
    
    
    

}
