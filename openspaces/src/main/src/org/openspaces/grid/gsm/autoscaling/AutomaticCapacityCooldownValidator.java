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

import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingTemporarilyDisabledCooldownException;

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
    private Long cooldownExpiredTimestamp = 0L;
    
    public void setCooldownAfterInstanceRemoved(long period, TimeUnit timeUnit) {
        this.cooldownAfterInstanceRemovedMillis = timeUnit.toMillis(period);
    }
    
    public void setCooldownAfterInstanceAdded(long period, TimeUnit timeUnit) {
        this.cooldownAfterInstanceAddedMillis = timeUnit.toMillis(period);
    }
    
    /**
     * Raises exception if cooldown period is active.
     * Cooldown is active if an instance was removed or added 
     * and the preconfigured cooldown period has not passed since.
     * @throws AutoScalingTemporarilyDisabledCooldownException
     * 
     * @param existingInstancesUids - set of discovered processing unit instance UIDs
     */
    public void validate(Set<String> existingInstancesUids) throws AutoScalingTemporarilyDisabledCooldownException {
        validate(existingInstancesUids, System.currentTimeMillis());
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
    public void validate(Set<String> existingInstancesUids, long currentTimeMillis) throws AutoScalingTemporarilyDisabledCooldownException {
        
        updateCooldownTimestamp(existingInstancesUids, currentTimeMillis);

        if (cooldownExpiredTimestamp >= currentTimeMillis) {
            throw new AutoScalingTemporarilyDisabledCooldownException(cooldownExpiredTimestamp -currentTimeMillis);
        }
    }

    private void updateCooldownTimestamp(final Set<String> existingInstancesUids, long currentTimeMillis) {
        
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
        
        if (!addedInstancesUids.isEmpty()) {
            cooldownExpiredTimestamp = Math.max(cooldownExpiredTimestamp, currentTimeMillis + cooldownAfterInstanceAddedMillis);
            previousInstancesUids = existingInstancesUids;
        }
        
        if (!removedInstancesUids.isEmpty()) {
            cooldownExpiredTimestamp = Math.max(cooldownExpiredTimestamp, currentTimeMillis + cooldownAfterInstanceRemovedMillis);
            previousInstancesUids = existingInstancesUids;
        }
     }
}
