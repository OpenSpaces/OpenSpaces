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
package org.openspaces.admin.pu.events;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;

/**
 * A provision failure that occurred while trying to instantiate an instance on a grid service
 * container. Holds the {@link GridServiceContainer} the reason for the failure and an indication if
 * the instance will not be instantiated anymore.
 * 
 * @author moran
 * @since 8.0.6
 */
public class ProcessingUnitInstanceProvisionFailure {

    private final String failureReason;
    private final boolean uninstantiable;
    private final String gscServiceId;
    private final InternalProcessingUnit processingUnit;

    private GridServiceContainer cachedGridServiceContainer;
    
    public ProcessingUnitInstanceProvisionFailure(InternalProcessingUnit processingUnit, String gscServiceId,
            String failureReason, boolean uninstantiable) {
        this.processingUnit = processingUnit;
        this.gscServiceId = gscServiceId;
        this.failureReason = failureReason;
        this.uninstantiable = uninstantiable;
    }

    public GridServiceContainer getGridServiceContainer() {
        if (cachedGridServiceContainer == null && gscServiceId != null) {
            cachedGridServiceContainer = processingUnit.getAdmin().getGridServiceContainers().getContainerByUID(gscServiceId);
        }
        
        return cachedGridServiceContainer;
    }
    
    /**
     * The exception stack trace indicating that a failure has occurred while trying to instantiate a processing unit instance.
     * @return
     */
    public String getFailureReason() {
        return failureReason;
    }
    
    /**
     * Not instantiable; that cannot be instantiated. An uninstantiable instance will not be
     * re-provisioned again. This may be due to missing resources or classes, too many instances
     * versus required SLA. Note that the general case is that all instances should be
     * re-instantiable in order to keep the requested SLA for number of instances.
     * 
     * @return <code>true</code> if this instance was unable to instantiate and will not be
     *         re-provisioned again.
     */
    public boolean isUninstantiable() {
        return uninstantiable;
    }
}
