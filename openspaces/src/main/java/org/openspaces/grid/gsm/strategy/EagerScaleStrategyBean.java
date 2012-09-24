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
package org.openspaces.grid.gsm.strategy;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.zone.config.AnyZonesConfig;
import org.openspaces.grid.gsm.GridServiceContainerConfigAware;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementPendingProcessingUnitDeallocationException;
import org.openspaces.grid.gsm.machines.EagerMachinesSlaPolicy;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.exceptions.FailedToDiscoverMachinesException;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementPendingContainerDeallocationException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.NeedToWaitUntilAllGridServiceAgentsDiscoveredException;
import org.openspaces.grid.gsm.machines.exceptions.SomeProcessingUnitsHaveNotCompletedStateRecoveryException;
import org.openspaces.grid.gsm.machines.exceptions.UndeployInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.WaitingForDiscoveredMachinesException;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaPolicy;
import org.openspaces.grid.gsm.rebalancing.exceptions.RebalancingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

/**
 * The business logic that scales an elastic processing unit based on the specified
 * {@link EagerScaleConfig}
 * 
 * @author itaif
 * @since 8.0
 */
public class EagerScaleStrategyBean extends AbstractScaleStrategyBean 

    implements RebalancingSlaEnforcementEndpointAware , 
               ContainersSlaEnforcementEndpointAware, 
               MachinesSlaEnforcementEndpointAware,
               GridServiceContainerConfigAware {
    
    // injected 
    private EagerScaleConfig slaConfig;
    private MachinesSlaEnforcementEndpoint machinesEndpoint;
    private ContainersSlaEnforcementEndpoint containersEndpoint;
    private RebalancingSlaEnforcementEndpoint rebalancingEndpoint;
    private GridServiceContainerConfig containersConfig;

    public void setMachinesSlaEnforcementEndpoint(MachinesSlaEnforcementEndpoint endpoint) {
        this.machinesEndpoint = endpoint;
    }
    
    public void setContainersSlaEnforcementEndpoint(ContainersSlaEnforcementEndpoint containersService) {
        this.containersEndpoint = containersService;
    }
    
    public void setRebalancingSlaEnforcementEndpoint(RebalancingSlaEnforcementEndpoint relocationService) {
        this.rebalancingEndpoint = relocationService;
    }

    public void setGridServiceContainerConfig(GridServiceContainerConfig containersConfig) {
         this.containersConfig = containersConfig;
    }
    

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        
        if (machinesEndpoint == null) {
            throw new IllegalStateException("machines endpoint cannot be null.");
        }
        
        if (containersEndpoint == null) {
            throw new IllegalStateException("containers endpoint cannot be null");
        }
        
        if (rebalancingEndpoint == null) {
            throw new IllegalStateException("rebalancing endpoint cannot be null.");
        }
        
        slaConfig = new EagerScaleConfig(super.getProperties());
    }

    @Override
    public void enforceSla() throws SlaEnforcementInProgressException {
        
        SlaEnforcementInProgressException pendingException = null;

        final EagerMachinesSlaPolicy machinesSla = getMachinesSlaPolicy();
        try {
            enforceMachinesSla(machinesSla);
        }
        catch (GridServiceAgentSlaEnforcementPendingContainerDeallocationException e) {
            // fall through to containers sla enforcement since need to deallocate containers
            pendingException = e;
        }

        
        CapacityRequirementsPerAgent allocatedCapacity = machinesEndpoint.getAllocatedCapacity(machinesSla);
        try {
            enforceContainersSla(allocatedCapacity);
        }
        catch (ContainersSlaEnforcementPendingProcessingUnitDeallocationException e) {
            // fall through to rebalacing sla enforcement since need to deallocate pu instances
            pendingException = e;    
        }
        
        enforceRebalancingSla(allocatedCapacity, containersEndpoint.getContainers());
        
        if (pendingException != null) {
            throw pendingException;
        }
    }

    private void enforceMachinesSla(final EagerMachinesSlaPolicy sla) throws WaitingForDiscoveredMachinesException, GridServiceAgentSlaEnforcementInProgressException, FailedToDiscoverMachinesException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing machines SLA.");
        }

        try {
            machinesEndpoint.enforceSla(sla);
            
            machineProvisioningCompletedEvent(sla.getGridServiceAgentZones());
            agentProvisioningCompletedEvent(sla.getGridServiceAgentZones());

        }
        catch (GridServiceAgentSlaEnforcementInProgressException e) {
            
            machineProvisioningCompletedEvent(sla.getGridServiceAgentZones());
            agentProvisioningInProgressEvent(e, sla.getGridServiceAgentZones());
            throw e;
        }

    }

    private EagerMachinesSlaPolicy getMachinesSlaPolicy() {
        final EagerMachinesSlaPolicy sla = new EagerMachinesSlaPolicy();      
        sla.setMaximumNumberOfMachines(getMaximumNumberOfInstances());
        sla.setMinimumNumberOfMachines(getMinimumNumberOfMachines());
        sla.setMaximumNumberOfContainersPerMachine(getMaximumNumberOfContainersPerMachine());
        sla.setContainerMemoryCapacityInMB(containersConfig.getMaximumMemoryCapacityInMB());
        sla.setMachineIsolation(getIsolation());
        sla.setMachineProvisioning(super.getMachineProvisioning());
        sla.setDiscoveredMachinesCache(getDiscoveredMachinesCache());
        sla.setGridServiceAgentZones(new AnyZonesConfig());
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
            getLogger().debug("Containers Eager SLA Policy: "+
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
    
    private void enforceRebalancingSla(CapacityRequirementsPerAgent allocatedCapacity, GridServiceContainer[] containers) 
        throws RebalancingSlaEnforcementInProgressException 
    {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing rebalancing SLA.");
        }
        RebalancingSlaPolicy sla = new RebalancingSlaPolicy();
        sla.setContainers(containers);
        sla.setMaximumNumberOfConcurrentRelocationsPerMachine(slaConfig.getMaxConcurrentRelocationsPerMachine());
        sla.setSchemaConfig(getSchemaConfig());
        sla.setAllocatedCapacity(allocatedCapacity);
        sla.setMinimumNumberOfInstancesPerPartition(1);
        try {
            rebalancingEndpoint.enforceSla(sla);
            puInstanceProvisioningCompletedEvent();

        }
        catch (RebalancingSlaEnforcementInProgressException e) {
            puInstanceProvisioningInProgressEvent(e);
            throw e;
        }
    }
    
    @Override
    public EagerScaleConfig getConfig() {
        return slaConfig;
    }
    
    private int getMaximumNumberOfContainersPerMachine() {
        return slaConfig.isAtMostOneContainerPerMachine()?1:getMaximumNumberOfInstances();
    }

    @Override
    protected boolean isUndeploying() {
        return false;
    }

    @Override
    protected void recoverStateOnEsmStart() throws MachinesSlaEnforcementInProgressException, SomeProcessingUnitsHaveNotCompletedStateRecoveryException, NeedToWaitUntilAllGridServiceAgentsDiscoveredException, UndeployInProgressException {
        
        final EagerMachinesSlaPolicy sla = getMachinesSlaPolicy();
        machinesEndpoint.recoverStateOnEsmStart(sla);
        machinesEndpoint.recoveredStateOnEsmStart(getProcessingUnit());
    }

    @Override
    protected boolean isRecoveredStateOnEsmStart(ProcessingUnit otherPu) {
        return machinesEndpoint.isRecoveredStateOnEsmStart(otherPu);
    }
}
