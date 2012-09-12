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

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;

public class WrongContainerProcessingUnitRelocationException extends RebalancingSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1239832832671562407L;

    public WrongContainerProcessingUnitRelocationException(
            ProcessingUnitInstance instance, 
            GridServiceContainer expectedContainer) {
        
        super(instance.getProcessingUnit(),
                "Relocation of processing unit instance " + instance.getProcessingUnitInstanceName()+ " to container " +
                ContainersSlaUtils.gscToString(expectedContainer) + " "+
                "failed since the instance was eventually deployed on a different container " + 
                ContainersSlaUtils.gscToString(instance.getGridServiceContainer()));
    }

}
