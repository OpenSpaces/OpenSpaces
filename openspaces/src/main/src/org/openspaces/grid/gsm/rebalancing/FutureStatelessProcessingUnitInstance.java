package org.openspaces.grid.gsm.rebalancing;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.core.PollingFuture;

public interface FutureStatelessProcessingUnitInstance extends PollingFuture<ProcessingUnitInstance> {

    ProcessingUnit getProcessingUnit();
    
    GridServiceContainer getTargetContainer();
    
    /**
     * @return the exception or timeout error message.
     * @throws IllegalStateException - if the operation is in progress or completed successfully
     */
    String getFailureMessage() throws IllegalStateException ;

}
