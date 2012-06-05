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

import java.util.Map;
import java.util.Map.Entry;

import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyCapacityRequirementConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.grid.gsm.GridServiceContainerConfigAware;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.DriveCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;
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
    private ScaleStrategyCapacityRequirementConfig capacityRequirement;
    private ScaleStrategyConfig scaleStrategy;
    
    private long memoryInMB;
    
    /**
     * Call once in order to modify the behavior of {@link #enforceCapacityRequirement()}
     */
    protected void setCapacityRequirementConfig(ScaleStrategyCapacityRequirementConfig capacityRequirement) {
        if (capacityRequirement == null) {
            throw new IllegalArgumentException("capacityRequirement cannot be null");
        }
        this.capacityRequirement = capacityRequirement;
        this.memoryInMB = calcMemoryInMB();
    }
    
    protected ScaleStrategyCapacityRequirementConfig getCapacityRequirementConfig() {
        return capacityRequirement;
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

    protected void enforceCapacityRequirement() throws SlaEnforcementInProgressException {
        
        if (this.capacityRequirement == null) {
            throw new IllegalStateException("capacityRequirements cannot be null");
        }
        
        if (this.scaleStrategy == null) {
            throw new IllegalStateException("scaleStrategy cannot be null");
        }
        
        SlaEnforcementInProgressException pendingException = null;
        
        try {
            enforceMachinesSla();
        }
        catch (GridServiceAgentSlaEnforcementPendingContainerDeallocationException e) {
            // fall through to containers sla enforcement since need to scale-in containers
            pendingException = e;
        }
        
        
        try {
            enforceContainersSla();
        }
        catch (ContainersSlaEnforcementPendingProcessingUnitDeallocationException e) {
            // fall through to rebalacing sla enforcement since need to scale-in pu instances
            pendingException = e;
        }
            
        enforceRebalancingSla(containersEndpoint.getContainers());
    
        if (pendingException != null) {
            throw pendingException;
        }
    }

    private void enforceMachinesSla() 
            throws WaitingForDiscoveredMachinesException, 
                   MachinesSlaEnforcementInProgressException , GridServiceAgentSlaEnforcementInProgressException{
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing machines SLA.");
        }
        
        final CapacityMachinesSlaPolicy sla = new CapacityMachinesSlaPolicy();
        sla.setMachineProvisioning(super.getMachineProvisioning());
        CapacityRequirements capacityRequirements = new CapacityRequirements(
                new CpuCapacityRequirement(capacityRequirement.getNumberOfCpuCores()),
                new MemoryCapacityRequirement(memoryInMB));
        Map<String, Long> drivesCapacityInMB = capacityRequirement.getDrivesCapacityInMB();
        for (Entry<String, Long> pair : drivesCapacityInMB.entrySet()) {
            String drive = pair.getKey();
            capacityRequirements = capacityRequirements.add(
                    new DriveCapacityRequirement(drive,pair.getValue()));
        }
        sla.setCapacityRequirements(capacityRequirements);
        sla.setMinimumNumberOfMachines(getMinimumNumberOfMachines());
        sla.setMaximumNumberOfMachines(getMaximumNumberOfInstances());
        sla.setMaximumNumberOfContainersPerMachine(getMaximumNumberOfContainersPerMachine());
        sla.setContainerMemoryCapacityInMB(containersConfig.getMaximumMemoryCapacityInMB());
        sla.setMachineIsolation(getIsolation());
        sla.setDiscoveredMachinesCache(getDiscoveredMachinesCache());
        
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
    
    private void enforceContainersSla() throws ContainersSlaEnforcementInProgressException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing containers SLA.");
        }
        
        final ContainersSlaPolicy sla = new ContainersSlaPolicy();
        sla.setNewContainerConfig(containersConfig);
        sla.setClusterCapacityRequirements(machinesEndpoint.getAllocatedCapacity());
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
    
    private void enforceRebalancingSla(GridServiceContainer[] containers) throws RebalancingSlaEnforcementInProgressException 
    {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing rebalancing SLA.");
        }
        
        RebalancingSlaPolicy sla = new RebalancingSlaPolicy();
        sla.setContainers(containers);
        sla.setMaximumNumberOfConcurrentRelocationsPerMachine(scaleStrategy.getMaxConcurrentRelocationsPerMachine());
        sla.setSchemaConfig(getSchemaConfig());
        sla.setAllocatedCapacity(machinesEndpoint.getAllocatedCapacity());
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

    protected long calcMemoryInMB() {
        int targetNumberOfContainers = calcTargetNumberOfContainers();
        return targetNumberOfContainers * containersConfig.getMaximumMemoryCapacityInMB();    
    }
    
    private int calcTargetNumberOfContainers() {
        
        if (capacityRequirement.getMemoryCapacityInMB() > 0) {
            if (getSchemaConfig().isPartitionedSync2BackupSchema()) {
                return calcTargetNumberOfContainersForPartitionedSchema();
            } else {
                return calcTargetNumberOfContainersForStateless();
            }
        } else {
            return calcDefaultTargetNumberOfContainers();
        }
    }

    private int calcTargetNumberOfContainersForStateless() {
        int requiredNumberOfContainers = (int)Math.ceil(1.0 * capacityRequirement.getMemoryCapacityInMB() / containersConfig.getMaximumMemoryCapacityInMB());
        int targetNumberOfContainers = Math.max(
                getMinimumNumberOfMachines(),
                requiredNumberOfContainers);
        
        getLogger().info(
                "targetNumberOfContainers= "+
                "max(minimumNumberOfMachines, ceil(memory/jvm-size))= "+
                "max("+ 
                    getMinimumNumberOfMachines() + "," +
                    "ceil("+
                        capacityRequirement.getMemoryCapacityInMB() + "/" +
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
        double avgInstanceCapacityInMB = capacityRequirement.getMemoryCapacityInMB()/totalNumberOfInstances;
        getLogger().info(
                "instanceCapacityInMB= "+
                "memoryCapacityInMB/(numberOfInstances*(1+numberOfBackups))= "+
                capacityRequirement.getMemoryCapacityInMB()+"/"+totalNumberOfInstances+"= " +
                avgInstanceCapacityInMB);
        
        double containerCapacityInMB = containersConfig.getMaximumMemoryCapacityInMB();
        
        if (containerCapacityInMB < avgInstanceCapacityInMB) {
            throw new BeanConfigurationException(
                    "Reduce total capacity from " + capacityRequirement.getMemoryCapacityInMB() +"MB to " + containerCapacityInMB*totalNumberOfInstances+"MB. " +
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
        
        int targetNumberOfContainers = (int)Math.ceil(1.0 *capacityRequirement.getMemoryCapacityInMB() / containerCapacityInMB);
        getLogger().info(
                "targetNumberOfContainers= "+
                "ceil(memoryCapacity/containerCapacityInMB)= "+
                "ceil("+capacityRequirement.getMemoryCapacityInMB() +"/"+ containerCapacityInMB+") =" +
                targetNumberOfContainers);
        
        int numberOfBackups = getProcessingUnit().getNumberOfBackups();
        if (targetNumberOfContainers < numberOfBackups +1) {
         // raise exception if min number of containers conflicts with the specified memory capacity.
            int recommendedMemoryCapacityInMB = (int)((numberOfBackups +1) * containerCapacityInMB);
            throw new BeanConfigurationException(
                    targetNumberOfContainers + " containers are needed in order to scale to " + capacityRequirement.getMemoryCapacityInMB() + "m , "+
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
    
}
