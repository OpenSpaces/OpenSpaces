/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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
