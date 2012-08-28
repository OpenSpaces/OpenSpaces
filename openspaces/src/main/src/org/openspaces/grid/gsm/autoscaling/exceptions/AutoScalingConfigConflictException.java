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
package org.openspaces.grid.gsm.autoscaling.exceptions;

import java.util.Set;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerZones;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

/**
 * an exception that is raised if the autoscaling configuration parameters causes an un resolvable conflict 
 * in the Minimum/Maximum Capacity requirements for a specific zone.
 * @author elip
 *
 */
public class AutoScalingConfigConflictException extends AutoScalingSlaEnforcementInProgressException 
    implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    private CapacityRequirements minimumCapacityRequirements;
    private CapacityRequirements maximumCapacityRequirements;
    private Set<String> zones;
    
    public AutoScalingConfigConflictException(ProcessingUnit pu, CapacityRequirements minimum, 
            CapacityRequirements maximum, Set<String> zones, CapacityRequirementsPerZones lastEnforcedCapacityPerZones,
            CapacityRequirementsPerZones newCapacityRequirementsPerZones) {
        super(pu, message(minimum, maximum, zones, lastEnforcedCapacityPerZones, newCapacityRequirementsPerZones));
        this.minimumCapacityRequirements = minimum;
        this.maximumCapacityRequirements = maximum;
        this.zones = zones;
    }
    
    private static String message(CapacityRequirements minimum, CapacityRequirements maximum,Set<String> zones,
            CapacityRequirementsPerZones lastEnforcedCapacityPerZones,
            CapacityRequirementsPerZones newCapacityRequirementsPerZones) {
        return "Configuration Confilict. autoscaling will not continue. minimumCapacityRequirements= " + minimum + " is in conflict with maximumCapacityRequirements="
                    + maximum + " for zones " + zones + " : lastEnforcedCapacityPerZones=" + lastEnforcedCapacityPerZones + " , newCapacityRequirementsPerZones=" + newCapacityRequirementsPerZones;
        
        
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((maximumCapacityRequirements == null) ? 0 : maximumCapacityRequirements.hashCode());
        result = prime * result + ((minimumCapacityRequirements == null) ? 0 : minimumCapacityRequirements.hashCode());
        result = prime * result + ((zones == null) ? 0 : zones.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        AutoScalingConfigConflictException other = (AutoScalingConfigConflictException) obj;
        if (maximumCapacityRequirements == null) {
            if (other.maximumCapacityRequirements != null)
                return false;
        } else if (!maximumCapacityRequirements.equals(other.maximumCapacityRequirements))
            return false;
        if (minimumCapacityRequirements == null) {
            if (other.minimumCapacityRequirements != null)
                return false;
        } else if (!minimumCapacityRequirements.equals(other.minimumCapacityRequirements))
            return false;
        if (zones == null) {
            if (other.zones != null)
                return false;
        } else if (!zones.equals(other.zones))
            return false;
        return true;
    }
    
    
}
