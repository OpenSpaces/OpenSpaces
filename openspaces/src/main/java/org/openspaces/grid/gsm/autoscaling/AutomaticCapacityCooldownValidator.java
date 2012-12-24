/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.grid.gsm.autoscaling;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingTemporarilyDisabledCooldownException;

import com.j_spaces.kernel.time.SystemTime;

/**
 * Validates the cooldown period of auto-scaling rules.
 * If an instance was added the increase cooldown period is in place
 * If an instance was removed the decrease cooldown period is in place
 * @author itaif
 * @since 9.0.0
 */
public class AutomaticCapacityCooldownValidator {

    //configuration
    private long cooldownAfterInstanceRemovedMillis;
    private long cooldownAfterInstanceAddedMillis;
    
    //state
    private Set<String> previousInstancesUids;
    private DeploymentStatus previousDeploymentStatus;
    private Long cooldownExpiredTimestamp = 0L;
    private InternalProcessingUnit processingUnit;
    
    public void setCooldownAfterInstanceRemoved(long period, TimeUnit timeUnit) {
        this.cooldownAfterInstanceRemovedMillis = timeUnit.toMillis(period);
    }
    
    public void setCooldownAfterInstanceAdded(long period, TimeUnit timeUnit) {
        this.cooldownAfterInstanceAddedMillis = timeUnit.toMillis(period);
    }
    
    public void setProcessingUnit(InternalProcessingUnit processingUnit) {
        this.processingUnit = processingUnit;
    }
    
    
    /**
     * Raises exception if cooldown period is active.
     * Cooldown is active if an instance was removed or added 
     * and the preconfigured cooldown period has not passed since.
     * @throws AutoScalingTemporarilyDisabledCooldownException
     */
    public void validate() throws AutoScalingTemporarilyDisabledCooldownException {
        validate(processingUnit.getStatus(), getInstancesUids(), SystemTime.timeMillis());
    }
    
    /**
     * Raises exception if cooldown period is active.
     * Cooldown is active if an instance was removed or added 
     * and the preconfigured cooldown period has not passed since.
     * @throws AutoScalingTemporarilyDisabledCooldownException
     * 
     * @param existingInstancesUids - set of discovered processing unit instance UIDs
     * @param currentTimeMillis - current time in milliseconds 
     */
    public void validate(DeploymentStatus deploymentStatus, Set<String> existingInstancesUids, long currentTimeMillis) throws AutoScalingTemporarilyDisabledCooldownException {
        
        updateCooldownTimestamp(deploymentStatus, existingInstancesUids, currentTimeMillis);

        if (cooldownExpiredTimestamp >= currentTimeMillis) {
            throw new AutoScalingTemporarilyDisabledCooldownException(processingUnit, cooldownExpiredTimestamp -currentTimeMillis);
        }
    }

    private void updateCooldownTimestamp(DeploymentStatus existingDeploymentStatus, final Set<String> existingInstancesUids, long currentTimeMillis) {
        
        Set<String> addedInstancesUids = new HashSet<String>();
        addedInstancesUids.addAll(existingInstancesUids);
        if (previousInstancesUids != null) {
            addedInstancesUids.removeAll(previousInstancesUids);
        }
                
        Set<String> removedInstancesUids = new HashSet<String>();
        if (previousInstancesUids != null) {
            removedInstancesUids.addAll(previousInstancesUids);
            removedInstancesUids.removeAll(existingInstancesUids);
        }
        
        boolean isInstanceJustStarted = 
                addedInstancesUids.isEmpty() && 
                removedInstancesUids.isEmpty() && 
                (previousDeploymentStatus != DeploymentStatus.INTACT && 
                existingDeploymentStatus==DeploymentStatus.INTACT);
        
        if (!addedInstancesUids.isEmpty() || 
            isInstanceJustStarted) {
            cooldownExpiredTimestamp = Math.max(cooldownExpiredTimestamp, currentTimeMillis + cooldownAfterInstanceAddedMillis);
            previousInstancesUids = existingInstancesUids;
            previousDeploymentStatus = existingDeploymentStatus;
        }
        
        if (!removedInstancesUids.isEmpty()) {
            cooldownExpiredTimestamp = Math.max(cooldownExpiredTimestamp, currentTimeMillis + cooldownAfterInstanceRemovedMillis);
            previousInstancesUids = existingInstancesUids;
            previousDeploymentStatus = existingDeploymentStatus;
        }
        
     }

    
    private Set<String> getInstancesUids() {
        Set<String> instanceUids = new HashSet<String>();
        for (ProcessingUnitInstance instance : processingUnit) {
            instanceUids.add(instance.getUid());
        }
        return instanceUids;
    }
    
}
