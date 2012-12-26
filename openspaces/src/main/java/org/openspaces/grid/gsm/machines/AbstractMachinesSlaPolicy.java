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
package org.openspaces.grid.gsm.machines;

import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;
import org.openspaces.grid.gsm.strategy.DiscoveredMachinesCache;

public abstract class AbstractMachinesSlaPolicy extends ServiceLevelAgreementPolicy{

    private int maxNumberOfMachines;
    private int minimumNumberOfMachines;
    private long containerMemoryCapacityInMB;
    private NonBlockingElasticMachineProvisioning machineProvisioning;
    private ElasticProcessingUnitMachineIsolation machineIsolation;
    private int maxNumberOfContainersPerMachine;
    private DiscoveredMachinesCache machinesCache;
    private ZonesConfig zones;

    public ZonesConfig getGridServiceAgentZones() {
        return zones;
    }

    public void setGridServiceAgentZones(ZonesConfig zones) {
        this.zones = zones;
    }
    
    public DiscoveredMachinesCache getDiscoveredMachinesCache() {
        return this.machinesCache;
    }
    
    public void setDiscoveredMachinesCache(DiscoveredMachinesCache machinesCache) {
        this.machinesCache = machinesCache;
    }

    public int getMinimumNumberOfMachines() {
        return minimumNumberOfMachines;
    }
    
    public void setMinimumNumberOfMachines(int minimumNumberOfMachines) {
        this.minimumNumberOfMachines = minimumNumberOfMachines;
    }
    
    public long getReservedMemoryCapacityPerMachineInMB() {
        return MachinesSlaUtils.getMemoryInMB(getReservedCapacityPerMachine());
    }
    
    public void setContainerMemoryCapacityInMB(long containerMemoryCapacityInMB) {
        this.containerMemoryCapacityInMB = containerMemoryCapacityInMB;       
    }
    
    public long getContainerMemoryCapacityInMB() {
        return this.containerMemoryCapacityInMB;
    }

    public NonBlockingElasticMachineProvisioning getMachineProvisioning() {
        return this.machineProvisioning;
    }
    
    public void setMachineProvisioning(NonBlockingElasticMachineProvisioning machineProvisioning) {
        this.machineProvisioning = machineProvisioning;
    }

    public void setMaximumNumberOfMachines(int maxNumberOfMachines) {
        this.maxNumberOfMachines = maxNumberOfMachines;
    }
    
    public int getMaximumNumberOfMachines() {
        return this.maxNumberOfMachines;
    }

    public ElasticProcessingUnitMachineIsolation getMachineIsolation() {
        return machineIsolation;
    }
    
    public void setMachineIsolation(ElasticProcessingUnitMachineIsolation isolation) {
        this.machineIsolation = isolation;
    }

    public CapacityRequirements getReservedCapacityPerMachine() {
        return machineProvisioning.getConfig().getReservedCapacityPerMachine();
    }


    public void setMaximumNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine) {
        this.maxNumberOfContainersPerMachine = maxNumberOfContainersPerMachine;
    }
    
    public int getMaximumNumberOfContainersPerMachine() {
        return maxNumberOfContainersPerMachine;
    }

    public boolean isUndeploying() {
        return false;
    }

    @Override
    public void validate() {
        
        if (this.containerMemoryCapacityInMB <= 0) {
            throw new IllegalArgumentException("Container memory capacity must be defined.");
        }
        
        if (this.machineProvisioning == null) {
            throw new IllegalArgumentException("machine provisioning cannot be null");
        }
        
        if (machineIsolation == null) {
            throw new IllegalArgumentException("machine isolation cannot be null");
        }
        
        if (this.maxNumberOfMachines < 0) {
            throw new IllegalArgumentException("maximum number of machines cannot be " + getMaximumNumberOfMachines());
        }
        
        if (minimumNumberOfMachines < 0) {
            throw new IllegalArgumentException("minimum number of machines cannot be " + getMinimumNumberOfMachines());
        }
        
        if (minimumNumberOfMachines > this.maxNumberOfMachines) {
            throw new IllegalArgumentException(
                    "minimum number of machines ("+getMinimumNumberOfMachines()+
                    ") cannot be bigger than maximum number of machines ("+
                    getMaximumNumberOfMachines()+")");
        }
        
        if (this.machinesCache == null) {
            throw new IllegalArgumentException("Provisioned agents cannot be null");
        }
        
        if (this.getGridServiceAgentZones() == null) {
            throw new IllegalArgumentException("Exact Zones cannot be null in Capacity Machines Sla Policy");
        }
    }
    
    public abstract boolean isStopMachineSupported();

    public abstract String getScaleStrategyName();
}
