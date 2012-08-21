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
package org.openspaces.grid.gsm.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfig;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfigurer;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsPerZonesConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyCapacityRequirementConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.RequiredZonesConfig;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.core.util.MemoryUnit;
import org.openspaces.grid.gsm.GridServiceContainerConfigAware;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerZones;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementPendingProcessingUnitDeallocationException;
import org.openspaces.grid.gsm.machines.CapacityMachinesSlaPolicy;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementPendingContainerDeallocationException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.NeedToWaitUntilAllGridServiceAgentsDiscoveredException;
import org.openspaces.grid.gsm.machines.exceptions.PerZonesGridServiceAgentSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.PerZonesMachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.SomeProcessingUnitsHaveNotCompletedStateRecoveryException;
import org.openspaces.grid.gsm.machines.exceptions.WaitingForDiscoveredMachinesException;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaPolicy;
import org.openspaces.grid.gsm.rebalancing.exceptions.RebalancingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

/**
 * A class for code reuse between {@link ManualCapacityScaleStrategyBean}
 * and {@link AutomaticCapacityScaleStrategyBean}
 * @author itaif
 * @since 9.0.0
 */
public abstract class AbstractCapacityScaleStrategyBean extends AbstractScaleStrategyBean 
    implements 
    GridServiceContainerConfigAware,
    RebalancingSlaEnforcementEndpointAware , 
    ContainersSlaEnforcementEndpointAware, 
    MachinesSlaEnforcementEndpointAware {

    private GridServiceContainerConfig containersConfig;
    private MachinesSlaEnforcementEndpoint machinesEndpoint;
    private ContainersSlaEnforcementEndpoint containersEndpoint;
    private RebalancingSlaEnforcementEndpoint rebalancingEndpoint;
    
    @Override
    public void setMachinesSlaEnforcementEndpoint(MachinesSlaEnforcementEndpoint machinesService) {
        this.machinesEndpoint = machinesService;
    }

    @Override
    public void setContainersSlaEnforcementEndpoint(ContainersSlaEnforcementEndpoint containersService) {
        this.containersEndpoint = containersService;
    }
    
    @Override
    public void setRebalancingSlaEnforcementEndpoint(RebalancingSlaEnforcementEndpoint relocationService) {
        this.rebalancingEndpoint = relocationService;
    }

    // created by afterPropertiesSet()
    private CapacityRequirementsPerZonesConfig capacityPerZones;
    private ScaleStrategyConfig scaleStrategy;
        
    protected ZonesConfig getDefaultZones() {
        return getMachineProvisioning().getConfig().getGridServiceAgentZones();
    }
    
    protected void setPlannedCapacity(CapacityRequirementsPerZones plannedCapacity) {
        setPlannedCapacity(new CapacityRequirementsPerZonesConfig(plannedCapacity));
    }    

    /**
     * Call once in order to modify the behavior of {@link #enforcePlannedCapacity()}
     * Uses the default machine provisioning zone as the zone to start machines in.
     */   
    protected void setPlannedCapacity(ScaleStrategyCapacityRequirementConfig capacity) {
        
        final CapacityRequirementsPerZonesConfig capacityPerZones = new CapacityRequirementsPerZonesConfig();
        if (!capacity.toCapacityRequirements().equalsZero()) {
            capacityPerZones.addCapacity(getDefaultZones(), capacity);
        }
        
        setPlannedCapacity(capacityPerZones);     
    }

    /**
     * Call once in order to modify the behavior of {@link #enforcePlannedCapacity()}
     */   
    protected void setPlannedCapacity(CapacityRequirementsPerZonesConfig capacityPerZones) {
        
        if (capacityPerZones == null) {
            throw new IllegalArgumentException("capacityRequirement cannot be null");
        }
        
        if (this.capacityPerZones != null && capacityPerZones.toCapacityRequirementsPerZones().equals(this.capacityPerZones.toCapacityRequirementsPerZones())) {
            return;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Setting planned capacity to " + capacityPerZones + " (old planned capacity = " + this.capacityPerZones + ")");
        }
        this.capacityPerZones = capacityPerZones;
        
        // round up memory
        long roundedMemoryInMB = calcRoundedTotalMemoryInMB();
        long totalMemoryInMB = getTotalMemoryInMB();
        if (totalMemoryInMB > roundedMemoryInMB) {
            throw new IllegalStateException("totalMemoryInMB ("+totalMemoryInMB +") cannot be bigger than roundedTotalMemoryInMB ("+ roundedMemoryInMB +")");
        }
        if (totalMemoryInMB < roundedMemoryInMB) {
            long memoryShortage = roundedMemoryInMB - totalMemoryInMB;
            CapacityRequirementsConfig increase = new CapacityRequirementsConfigurer()
            .memoryCapacity((int)memoryShortage, MemoryUnit.MEGABYTES)
            .create();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Increasing planned capacity by " + increase + " to round up memory to nearest number of containers.");
            }
            capacityPerZones.addCapacity(
                    getDefaultZones(), 
                    increase
            );
        }
    }
    
    /**
     * Calculates the minimum number of machines per zone
     * Based on number of zones and total minimum number of machines
     * @throws MachinesSlaEnforcementInProgressException 
     */
    private Map<ZonesConfig, Integer> calcMinimumNumberOfMachinesPerZones() throws MachinesSlaEnforcementInProgressException {

        Map<ZonesConfig, Integer> minimumNumberOfMachinesPerZone = new HashMap<ZonesConfig,Integer>();
        
        final int minimumTotalNumberOfMachines = getMinimumNumberOfMachines();
        
        final Collection<ZonesConfig> plannedZones = getPlannedZones();
        
        int defaultNumberOfMachines = (int) Math.ceil(1.0*minimumTotalNumberOfMachines/plannedZones.size());
        
        int remainingNumberOfMachines = minimumTotalNumberOfMachines;
        for (ZonesConfig zones : plannedZones) {
            
            int numberOfMachines = 0;
            if (plannedZones.contains(zones)) {
                numberOfMachines = Math.min(defaultNumberOfMachines, remainingNumberOfMachines);
            }
            minimumNumberOfMachinesPerZone.put(zones, numberOfMachines);
            remainingNumberOfMachines -= numberOfMachines;
        }
        
        for (ZonesConfig zones : machinesEndpoint.getGridServiceAgentsZones()) {
            if (!plannedZones.contains(zones)) {
                minimumNumberOfMachinesPerZone.put(zones, 0);
            }
        }
        
        return minimumNumberOfMachinesPerZone;
    }

    protected CapacityRequirementsPerZonesConfig getCapacityRequirementConfig() {
        return capacityPerZones;
    }
    
    protected void setScaleStrategyConfig(ScaleStrategyConfig scaleStrategy) {
        this.scaleStrategy = scaleStrategy;
    }
    
    private int getMaximumNumberOfContainersPerMachine() {
        return scaleStrategy.isAtMostOneContainerPerMachine()?1:getMaximumNumberOfInstances();
    }
    
    @Override
    public void setGridServiceContainerConfig(GridServiceContainerConfig containersConfig) {
        this.containersConfig = containersConfig;
    }
    
    public GridServiceContainerConfig getGridServiceContainerConfig() {
        return this.containersConfig;
    }

    @Override
    public void afterPropertiesSet() {
        
        if (machinesEndpoint == null) {
            throw new IllegalStateException("machines endpoint cannot be null.");
        }
        
        if (containersEndpoint == null) {
            throw new IllegalStateException("containers endpoint cannot be null");
        }
        
        if (rebalancingEndpoint == null) {
            throw new IllegalStateException("rebalancing endpoint cannot be null.");
        }
        
        super.afterPropertiesSet();
    }

    protected void enforcePlannedCapacity() throws SlaEnforcementInProgressException {
        
        if (this.capacityPerZones == null) {
            throw new IllegalStateException("capacityPerZones cannot be null");
        }
        
        if (this.scaleStrategy == null) {
            throw new IllegalStateException("scaleStrategy cannot be null");
        }
        
        CapacityRequirementsPerAgent totalAllocatedCapacity = new CapacityRequirementsPerAgent();
        PerZonesMachinesSlaEnforcementInProgressException pendingMachinesExceptions = new PerZonesMachinesSlaEnforcementInProgressException(new String[]{getProcessingUnit().getName()});
        PerZonesGridServiceAgentSlaEnforcementInProgressException pendingAgentsExceptions = new PerZonesGridServiceAgentSlaEnforcementInProgressException(new String[]{getProcessingUnit().getName()});
        ContainersSlaEnforcementInProgressException pendingContainersException = null;
        RebalancingSlaEnforcementInProgressException pendingRebalancingException = null;
        
        try {
        for (CapacityMachinesSlaPolicy sla : getMachinesSlas(getAllZones())) {
            
            ZonesConfig zones = sla.getGridServiceAgentZones();
            try {
                enforceMachinesSla(sla);
                if (isGridServiceAgentZonesAware()) {
                    CapacityRequirementsPerAgent allocatedCapacity = 
                            machinesEndpoint.getAllocatedCapacity(sla);
                    replacePlannedCapacityForZones(zones, allocatedCapacity);
                }
            }
            catch (GridServiceAgentSlaEnforcementPendingContainerDeallocationException e) {
                // fall through to containers sla enforcement since need to scale-in containers
                pendingAgentsExceptions.addReason(zones,e);
            }
            catch (GridServiceAgentSlaEnforcementInProgressException e) {
                if (getSchemaConfig().isPartitionedSync2BackupSchema()) {
                    // do not support space rebalancing
                    // unless all GSAs are available on all zones
                    throw e;
                }
                // handle next zone
                pendingAgentsExceptions.addReason(zones,e);
            }
            catch (MachinesSlaEnforcementInProgressException e) {
                if (getSchemaConfig().isPartitionedSync2BackupSchema()) {
                    // do not support space rebalancing
                    // unless all GSAs are available on all zones
                    throw e;
                }
                // handle next zone
                pendingMachinesExceptions.addReason(zones,e);
            }
            CapacityRequirementsPerAgent allocatedCapacity = machinesEndpoint.getAllocatedCapacity(sla);
            totalAllocatedCapacity = totalAllocatedCapacity.add(allocatedCapacity);
        }//for
        
        try {
            enforceContainersSla(totalAllocatedCapacity);
        }
        catch (ContainersSlaEnforcementPendingProcessingUnitDeallocationException e) {
            // fall through to rebalacing sla enforcement since need to scale-in pu instances
            pendingContainersException = e;
        }

        try {
            enforceRebalancingSla(containersEndpoint.getContainers(), totalAllocatedCapacity);
        }
        catch (RebalancingSlaEnforcementInProgressException e) {
            pendingRebalancingException = e;
        }
        
        if (pendingRebalancingException != null) {
            logSwallowedExceptions(pendingMachinesExceptions,pendingAgentsExceptions,pendingContainersException);
            throw pendingRebalancingException;
        }
        
        if (pendingContainersException != null) {
            logSwallowedExceptions(pendingMachinesExceptions,pendingAgentsExceptions);
            throw pendingContainersException;
        }
        
        if (pendingAgentsExceptions.hasReason()) {
            logSwallowedExceptions(pendingMachinesExceptions);
            throw pendingAgentsExceptions;
        }
        
        if (pendingMachinesExceptions.hasReason()) {
            throw pendingMachinesExceptions;
        }
        }
        catch (RuntimeException e) {
            logSwallowedExceptions(pendingMachinesExceptions,pendingAgentsExceptions,pendingContainersException,pendingRebalancingException);
            throw e;
        }
        catch (Error e) {
            logSwallowedExceptions(pendingMachinesExceptions,pendingAgentsExceptions,pendingContainersException,pendingRebalancingException);
            throw e;
        }
    }
    
    private void logSwallowedExceptions(
            PerZonesMachinesSlaEnforcementInProgressException pendingMachinesExceptions,
            PerZonesGridServiceAgentSlaEnforcementInProgressException pendingAgentsExceptions) {
        logSwallowedExceptions(pendingMachinesExceptions, pendingAgentsExceptions, null);
        
    }

    private void logSwallowedExceptions(
            PerZonesMachinesSlaEnforcementInProgressException pendingMachinesExceptions,
            PerZonesGridServiceAgentSlaEnforcementInProgressException pendingAgentsExceptions,
            ContainersSlaEnforcementInProgressException pendingContainersException) {
        logSwallowedExceptions(pendingMachinesExceptions,pendingAgentsExceptions,pendingContainersException,null);
    }

    private void logSwallowedExceptions(PerZonesMachinesSlaEnforcementInProgressException pendingMachinesExceptions) {
        logSwallowedExceptions(pendingMachinesExceptions,null);
    }

    private void logSwallowedExceptions(
            PerZonesMachinesSlaEnforcementInProgressException pendingMachinesExceptions,
            PerZonesGridServiceAgentSlaEnforcementInProgressException pendingAgentsExceptions,
            ContainersSlaEnforcementInProgressException pendingContainersException,
            RebalancingSlaEnforcementInProgressException pendingRebalancingException) {
        
        if (getLogger().isInfoEnabled()) {
            if (pendingMachinesExceptions.hasReason()) {
                getLogger().info("Swallowed exception",pendingMachinesExceptions);
            }
            if (pendingAgentsExceptions.hasReason()) {
                getLogger().info("Swallowed exception",pendingAgentsExceptions);
            }
            if (pendingContainersException != null) {
                getLogger().info("Swallowed exception",pendingContainersException);
            }
            if (pendingRebalancingException != null) {
                getLogger().info("Swallowed exception",pendingRebalancingException);
            }
        }
        
    }

    protected abstract boolean isGridServiceAgentZonesAware();

    /**
     * Replaces the planned capacity of the specified zones, with the specified new planned capacity
     * which could be with other zones
     */
    private void replacePlannedCapacityForZones(ZonesConfig zonesToRemove, CapacityRequirementsPerAgent capacityToAdd) {

        if (getLogger().isDebugEnabled()) {
            if (zonesToRemove instanceof RequiredZonesConfig) {
                //most likely first time this happened
                getLogger().debug("Replacing zones " + zonesToRemove + " with " + capacityToAdd);
            }
        }
        CapacityRequirementsPerZones newCapacityPerZones = 
                capacityPerZones.toCapacityRequirementsPerZones()
                .subtractZones(zonesToRemove);
        
        for (String agentUid : capacityToAdd.getAgentUids()) {
            
            GridServiceAgent agent = getAdmin().getGridServiceAgents().getAgentByUID(agentUid);
            CapacityRequirements agentCapacity = capacityToAdd.getAgentCapacity(agentUid);
            ExactZonesConfig zones = agent.getExactZones();
            newCapacityPerZones = newCapacityPerZones.add(zones, agentCapacity);
        }
        setPlannedCapacity(new CapacityRequirementsPerZonesConfig(newCapacityPerZones));
    }

    private Set<ZonesConfig> getAllZones()
            throws MachinesSlaEnforcementInProgressException {
        final Set<ZonesConfig> allZones = new HashSet<ZonesConfig>();
        allZones.addAll(machinesEndpoint.getGridServiceAgentsZones());
        allZones.addAll(getPlannedZones());
        return allZones;
    }

    private Set<ZonesConfig> getPlannedZones() {
        return this.capacityPerZones.toCapacityRequirementsPerZones().getZones();
    }
 
    private void enforceMachinesSla(CapacityMachinesSlaPolicy sla)  
            throws WaitingForDiscoveredMachinesException, 
                   MachinesSlaEnforcementInProgressException , GridServiceAgentSlaEnforcementInProgressException{
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing machines SLA.");
        }
        
        try {
            machinesEndpoint.enforceSla(sla);
            machineProvisioningCompletedEvent();
            agentProvisioningCompletedEvent();
        }
        catch (MachinesSlaEnforcementInProgressException e) {
            
            machineProvisioningInProgressEvent(e);
            throw e;
        }
        catch (GridServiceAgentSlaEnforcementInProgressException e) {
         
            machineProvisioningCompletedEvent();
            agentProvisioningInProgressEvent(e);
            throw e;
        }
    }

    private CapacityMachinesSlaPolicy getMachinesSla(ZonesConfig zones, int minimumNumberOfMachines,
            CapacityRequirements capacityRequirements) {
        final CapacityMachinesSlaPolicy sla = new CapacityMachinesSlaPolicy();
        sla.setMachineProvisioning(super.getMachineProvisioning());
        sla.setCapacityRequirements(capacityRequirements);
        sla.setMinimumNumberOfMachines(minimumNumberOfMachines);
        sla.setMaximumNumberOfMachines(getMaximumNumberOfInstances());
        sla.setMaximumNumberOfContainersPerMachine(getMaximumNumberOfContainersPerMachine());
        sla.setContainerMemoryCapacityInMB(containersConfig.getMaximumMemoryCapacityInMB());
        sla.setMachineIsolation(getIsolation());
        sla.setDiscoveredMachinesCache(getDiscoveredMachinesCache());
        sla.setGridServiceAgentZones(zones);
        return sla;
    }
    
    private void enforceContainersSla(CapacityRequirementsPerAgent allocatedCapacity) throws ContainersSlaEnforcementInProgressException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing containers SLA.");
        }
        
        final ContainersSlaPolicy sla = new ContainersSlaPolicy();
        sla.setNewContainerConfig(containersConfig);
        sla.setClusterCapacityRequirements(allocatedCapacity);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Containers Manual SLA Policy: "+
                    "#gridServiceAgents=" + sla.getClusterCapacityRequirements().getAgentUids().size() + " "+
                    "newContainerConfig.maximumMemoryCapacityInMB="+sla.getNewContainerConfig().getMaximumMemoryCapacityInMB());
        }
        try {
            containersEndpoint.enforceSla(sla);
            containerProvisioningCompletedEvent();
        }
        catch (ContainersSlaEnforcementInProgressException e) {
            containerProvisioningInProgressEvent(e);
            throw e;
        }
    }
    
    private void enforceRebalancingSla(GridServiceContainer[] containers, CapacityRequirementsPerAgent allocatedCapacity) throws RebalancingSlaEnforcementInProgressException 
    {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing rebalancing SLA.");
        }
        
        RebalancingSlaPolicy sla = new RebalancingSlaPolicy();
        sla.setContainers(containers);
        sla.setMaximumNumberOfConcurrentRelocationsPerMachine(scaleStrategy.getMaxConcurrentRelocationsPerMachine());
        sla.setSchemaConfig(getSchemaConfig());
        sla.setAllocatedCapacity(allocatedCapacity);
        if (getSchemaConfig().isDefaultSchema() && scaleStrategy instanceof AutomaticCapacityScaleConfig) {
            //make sure that number of instances does not go below the specified minimum, even when relocations are required.
            AutomaticCapacityScaleConfig automaticCapacityScaleConfig = (AutomaticCapacityScaleConfig) scaleStrategy;
            int minimumNumberOfInstancesPerPartition = (int)(automaticCapacityScaleConfig.getMinCapacity().getMemoryCapacityInMB() / containersConfig.getMaximumMemoryCapacityInMB());
            sla.setMinimumNumberOfInstancesPerPartition(minimumNumberOfInstancesPerPartition);
        }
        else {
            sla.setMinimumNumberOfInstancesPerPartition(1);
        }
        try {
            rebalancingEndpoint.enforceSla(sla);
            puInstanceProvisioningCompletedEvent();

        }
        catch (RebalancingSlaEnforcementInProgressException e) {
            puInstanceProvisioningInProgressEvent(e);
            throw e;
        }
    }

    protected long calcRoundedTotalMemoryInMB() {
        int targetNumberOfContainers = calcTargetNumberOfContainers();
        return targetNumberOfContainers * containersConfig.getMaximumMemoryCapacityInMB();    
    }
    
    private int calcTargetNumberOfContainers() {
        if (getTotalMemoryInMB() > 0) {
            if (getSchemaConfig().isPartitionedSync2BackupSchema()) {
                return calcTargetNumberOfContainersForPartitionedSchema();
            } else {
                return calcTargetNumberOfContainersForStateless();
            }
        } else {
            return calcDefaultTargetNumberOfContainers();
        }
    }

    private long getTotalMemoryInMB() {
        CapacityRequirementsConfig totalCapacity = new CapacityRequirementsConfig(capacityPerZones.toCapacityRequirementsPerZones().getTotalAllocatedCapacity());
        return totalCapacity.getMemoryCapacityInMB();
    }

    private int calcTargetNumberOfContainersForStateless() {
        int requiredNumberOfContainers = (int)Math.ceil(1.0 * getTotalMemoryInMB() / containersConfig.getMaximumMemoryCapacityInMB());
        int targetNumberOfContainers = Math.max(
                getMinimumNumberOfMachines(),
                requiredNumberOfContainers);
        
        getLogger().info(
                "targetNumberOfContainers= "+
                "max(minimumNumberOfMachines, ceil(memory/jvm-size))= "+
                "max("+ 
                    getMinimumNumberOfMachines() + "," +
                    "ceil("+
                        getTotalMemoryInMB() + "/" +
                        containersConfig.getMaximumMemoryCapacityInMB() + ")= " +
                "max("+ 
                    getMinimumNumberOfMachines() + "," + 
                    requiredNumberOfContainers+")= "+
                targetNumberOfContainers);
        
        return targetNumberOfContainers;
    }

    private int calcDefaultTargetNumberOfContainers() {
        
        int targetNumberOfContainers = Math.max(
                 getMinimumNumberOfMachines(),
                 getProcessingUnit().getNumberOfBackups()+1);
        getLogger().info(
                "targetNumberOfContainers= "+
                "max(minimumNumberOfMachines, numberOfBackupsPerParition+1)= "+
                "max("+ getMinimumNumberOfMachines() +","+1+ "+"+getProcessingUnit().getNumberOfBackups()+")= "+
                targetNumberOfContainers);
        
        return targetNumberOfContainers;
    }
    
    private int calcTargetNumberOfContainersForPartitionedSchema() {
        
        double totalNumberOfInstances = getProcessingUnit().getTotalNumberOfInstances();
        double avgInstanceCapacityInMB = getTotalMemoryInMB()/totalNumberOfInstances;
        getLogger().info(
                "instanceCapacityInMB= "+
                "memoryCapacityInMB/(numberOfInstances*(1+numberOfBackups))= "+
                getTotalMemoryInMB()+"/"+totalNumberOfInstances+"= " +
                avgInstanceCapacityInMB);
        
        double containerCapacityInMB = containersConfig.getMaximumMemoryCapacityInMB();
        
        if (containerCapacityInMB < avgInstanceCapacityInMB) {
            throw new BeanConfigurationException(
                    "Reduce total capacity from " + getTotalMemoryInMB() +"MB to " + containerCapacityInMB*totalNumberOfInstances+"MB. " +
                    "Container capacity is " + containerCapacityInMB+"MB , "+
                    "given " + totalNumberOfInstances + " instances, the total capacity =" 
                    +containerCapacityInMB+"MB *"+totalNumberOfInstances + "= " + 
                    containerCapacityInMB*totalNumberOfInstances+"MB. ");
                    
        }
        /*
         //this calculation does over provisioning of containers so all containers have the 
         //same number of instances. 
        double maxNumberOfInstancesPerContainer = Math.floor(containerCapacityInMB / instanceCapacityInMB); 
        getLogger().info(
                "maxNumberOfInstancesPerContainer= "+
                "floor(containerCapacityInMB/instanceCapacityInMB)= "+
                "floor("+containerCapacityInMB+"/"+instanceCapacityInMB+") =" +
                maxNumberOfInstancesPerContainer);
        
        int targetNumberOfContainers = (int) 
                Math.ceil(totalNumberOfInstances/ maxNumberOfInstancesPerContainer);
                
        getLogger().info(
                "targetNumberOfContainers= "+
                "ceil(totalNumberOfInstances/maxNumberOfInstancesPerContainer)= "+
                "ceil("+totalNumberOfInstances+"/"+maxNumberOfInstancesPerContainer+") =" +
                targetNumberOfContainers + " " +
                "Calculation enforces that each container has the same number of instances. "+
                "The total memory of all containers equals or bigger than the requested memory");
          */
        
        int targetNumberOfContainers = (int)Math.ceil(1.0 *getTotalMemoryInMB() / containerCapacityInMB);
        getLogger().info(
                "targetNumberOfContainers= "+
                "ceil(memoryCapacity/containerCapacityInMB)= "+
                "ceil("+getTotalMemoryInMB() +"/"+ containerCapacityInMB+") =" +
                targetNumberOfContainers);
        
        int numberOfBackups = getProcessingUnit().getNumberOfBackups();
        if (targetNumberOfContainers < numberOfBackups +1) {
         // raise exception if min number of containers conflicts with the specified memory capacity.
            int recommendedMemoryCapacityInMB = (int)((numberOfBackups +1) * containerCapacityInMB);
            throw new BeanConfigurationException(
                    targetNumberOfContainers + " containers are needed in order to scale to " + getTotalMemoryInMB() + "m , "+
                    "which cannot support " + (numberOfBackups==1?"one backup":numberOfBackups+" backups") + " per partition. "+
                    "Increase the memory capacity to " + recommendedMemoryCapacityInMB +"m");
        }
        
        if (targetNumberOfContainers == 0) {
            throw new IllegalStateException("targetNumberOfContainers cannot be zero");
        }
        return targetNumberOfContainers;
    }


    @Override
    protected boolean isUndeploying() {
        return false;
    }

    @Override
    protected void recoverStateOnEsmStart() throws MachinesSlaEnforcementInProgressException, SomeProcessingUnitsHaveNotCompletedStateRecoveryException, NeedToWaitUntilAllGridServiceAgentsDiscoveredException {
        
        for (CapacityMachinesSlaPolicy sla : getMachinesSlas(getPlannedZones())) {
            machinesEndpoint.recoverStateOnEsmStart(sla);
        }
        
        machinesEndpoint.recoveredStateOnEsmStart(getProcessingUnit());
    }

    private List<ZonesConfig> sortZonesByRecoveryOrder(Set<ZonesConfig> zoness) throws MachinesSlaEnforcementInProgressException {
        List<ZonesConfig> sortedZones = new ArrayList<ZonesConfig>(zoness);
        Collections.sort(sortedZones,new Comparator<ZonesConfig>() {

            @Override
            public int compare(ZonesConfig z1, ZonesConfig z2) {
                return rank(z1.getClass()) - rank(z2.getClass());
            }

            private int rank(Class<? extends ZonesConfig> clazz) {
                if (ExactZonesConfig.class.isAssignableFrom(clazz)) {
                    // max priority for exact zones definitions during ESM start recovery
                    return 0;
                }
                return 1;
            }
        });
        return sortedZones;
    }

    @Override
    protected boolean isRecoveredStateOnEsmStart(ProcessingUnit otherPu) {
        return machinesEndpoint.isRecoveredStateOnEsmStart(otherPu);
    }

    public CapacityRequirementsPerZones getAllocatedCapacity() throws MachinesSlaEnforcementInProgressException {
        CapacityRequirementsPerZones allocatedCapacityPerZones = new CapacityRequirementsPerZones(); 
        
        for (CapacityMachinesSlaPolicy sla : getMachinesSlas(machinesEndpoint.getGridServiceAgentsZones())) {
            CapacityRequirements allocatedCapacity = machinesEndpoint.getAllocatedCapacity(sla).getTotalAllocatedCapacity();
            if (!allocatedCapacity.equalsZero()) {
                allocatedCapacityPerZones.add(sla.getGridServiceAgentZones(), allocatedCapacity);
            }
        }
        return allocatedCapacityPerZones;
    }
    
    private List<CapacityMachinesSlaPolicy> getMachinesSlas(final Set<ZonesConfig> zoness) throws MachinesSlaEnforcementInProgressException {
        
        final List<CapacityMachinesSlaPolicy> machinesSlas = new ArrayList<CapacityMachinesSlaPolicy>();
        
        final Map<ZonesConfig,Integer> minimumNumberOfMachinesPerZone = calcMinimumNumberOfMachinesPerZones();
        final CapacityRequirementsPerZones capacityRequirementsPerZone = this.capacityPerZones.toCapacityRequirementsPerZones();
        final List<ZonesConfig> sortedZoness = sortZonesByRecoveryOrder(zoness);
        for (ZonesConfig zones : sortedZoness) {
            //TODO: Use NumberOfMachinesCapacityRequirement in capacityRequirements instead of minimumNumberOfMachines
            final Integer minimumNumberOfMachines = minimumNumberOfMachinesPerZone.get(zones);
            //could be zero due to requirements change but machines still running
            final CapacityRequirements capacityRequirements = capacityRequirementsPerZone.getZonesCapacityOrZero(zones);
            final CapacityMachinesSlaPolicy sla = getMachinesSla(zones, minimumNumberOfMachines, capacityRequirements);
            machinesSlas.add(sla);
        }        
        return machinesSlas;
    }
}
