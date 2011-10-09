package org.openspaces.grid.gsm.rebalancing.exceptions;

import java.util.Set;

import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.grid.gsm.rebalancing.RebalancingUtils;

public class SpaceRecoveryAfterRelocationException extends RebalancingSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;
    
    public SpaceRecoveryAfterRelocationException(
            ProcessingUnitInstance instance,
            Set<ProcessingUnitInstance> instancesFromSamePartition) {
        
        super(instance.getProcessingUnit(), otherInstancesMessage(instance, instancesFromSamePartition));
    }
    
    private static String otherInstancesMessage(ProcessingUnitInstance instance, Set<ProcessingUnitInstance> instancesFromSamePartition) {
        String errorMessage = 
            "Relocation of processing unit instance " + RebalancingUtils.puInstanceToString(instance) + " failed. "+
            "The following pu instance that were supposed to hold a copy of the data no longer exist :";
        for (ProcessingUnitInstance instanceFromSamePartition : instancesFromSamePartition) {
            errorMessage += " " + RebalancingUtils.puInstanceToString(instanceFromSamePartition);
        }
        return errorMessage;
    }
}
