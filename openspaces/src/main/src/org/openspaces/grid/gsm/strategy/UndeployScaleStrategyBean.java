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

import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.grid.gsm.GridServiceContainerConfigAware;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.containers.UndeployContainersSlaPolicy;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.CapacityMachinesSlaPolicy;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.UndeployMachinesSlaPolicy;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.NeedToWaitUntilAllGridServiceAgentsDiscoveredException;
import org.openspaces.grid.gsm.machines.exceptions.SomeProcessingUnitsHaveNotCompletedStateRecoveryException;
import org.openspaces.grid.gsm.machines.exceptions.WaitingForDiscoveredMachinesException;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.rebalancing.exceptions.ElasticProcessingUnitInstanceUndeployInProgress;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

/**
 * The business logic that undeploys an elastic processing unit
 * 
 * @author itaif
 * @since 8.0
 */
public class UndeployScaleStrategyBean extends AbstractScaleStrategyBean

    implements ContainersSlaEnforcementEndpointAware, 
               MachinesSlaEnforcementEndpointAware,
               GridServiceContainerConfigAware {

    // injected 
    private MachinesSlaEnforcementEndpoint machinesEndpoint;
    private ContainersSlaEnforcementEndpoint containersEndpoint;
    private GridServiceContainerConfig containersConfig;
    
    public void setMachinesSlaEnforcementEndpoint(MachinesSlaEnforcementEndpoint endpoint) {
        this.machinesEndpoint = endpoint;
    }
    
    public void setContainersSlaEnforcementEndpoint(ContainersSlaEnforcementEndpoint containersService) {
        this.containersEndpoint = containersService;
    }
    
    public void setGridServiceContainerConfig(GridServiceContainerConfig containersConfig) {
         this.containersConfig = containersConfig;
    }
    
    @Override
    public void afterPropertiesSet() {
        
        super.setMachineDiscoveryQuiteMode(true); // swallow machine provisioning errors while discovering agents
        
        super.afterPropertiesSet();
    
        if (machinesEndpoint == null) {
            throw new IllegalStateException("machines endpoint cannot be null.");
        }
        
        if (containersEndpoint == null) {
            throw new IllegalStateException("containers endpoint cannot be null");
        }
    }

    public void enforceSla() throws SlaEnforcementInProgressException {
        
        if (!isScaleInProgress()) {
            return;
        }
        
        if (getProcessingUnit().getInstances().length == 0) {
            puInstanceProvisioningCompletedEvent();
        }
        else {
            puInstanceProvisioningInProgressEvent(new ElasticProcessingUnitInstanceUndeployInProgress(getProcessingUnit()));
        }
        
        //proceed with container udeployment. It respects the pu instance download procedure.
        enforceContainersSla();
        for (final ExactZonesConfig zones : machinesEndpoint.getGridServiceAgentsZones()) {
            final CapacityMachinesSlaPolicy sla = getMachinesSlaPolicy(zones);
            enforceMachinesSla(sla);
        }
    }

    private void enforceMachinesSla(final CapacityMachinesSlaPolicy sla) throws WaitingForDiscoveredMachinesException, MachinesSlaEnforcementInProgressException, GridServiceAgentSlaEnforcementInProgressException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Undeploying machines for " + getProcessingUnit().getName());
        }
        
        try {
            machinesEndpoint.enforceSla(sla);
            
            machineProvisioningCompletedEvent();
            agentProvisioningCompletedEvent();
        } catch (MachinesSlaEnforcementInProgressException e) {
            machineProvisioningInProgressEvent(e);
            throw e;
        }
        catch (GridServiceAgentSlaEnforcementInProgressException e) {
            machineProvisioningCompletedEvent();
            agentProvisioningInProgressEvent(e);
            throw e;
        }
    }

    private CapacityMachinesSlaPolicy getMachinesSlaPolicy(ExactZonesConfig zones) {
        final CapacityMachinesSlaPolicy sla = new UndeployMachinesSlaPolicy(getAdmin());
        final NonBlockingElasticMachineProvisioning machineProvisioning = super.getMachineProvisioning();
        sla.setMachineProvisioning(machineProvisioning);
        sla.setMaximumNumberOfMachines(getMaximumNumberOfInstances());
        sla.setMaximumNumberOfContainersPerMachine(getMaximumNumberOfInstances());
        sla.setContainerMemoryCapacityInMB(containersConfig.getMaximumMemoryCapacityInMB());
        sla.setMachineIsolation(getIsolation());
        sla.setDiscoveredMachinesCache(getDiscoveredMachinesCache());
        sla.setExactZones(zones);
        return sla;
    }

    private void enforceContainersSla() throws ContainersSlaEnforcementInProgressException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Undeploying containers for " + getProcessingUnit().getName());
        }
        
        final ContainersSlaPolicy sla = new UndeployContainersSlaPolicy();
        sla.setNewContainerConfig(containersConfig);
        
        try {
            containersEndpoint.enforceSla(sla);
            containerProvisioningCompletedEvent();
        }
        catch (ContainersSlaEnforcementInProgressException e) {
            containerProvisioningInProgressEvent(e);
            throw e;
        }
    }
  

    public ScaleStrategyConfig getConfig() {
        return null;
    }

    @Override
    protected boolean isUndeploying() {
        return true;
    }

    @Override
    protected void recoverStateOnEsmStart() throws MachinesSlaEnforcementInProgressException, SomeProcessingUnitsHaveNotCompletedStateRecoveryException, NeedToWaitUntilAllGridServiceAgentsDiscoveredException {
        
        for (ExactZonesConfig zones : machinesEndpoint.getGridServiceAgentsZones()) {
            final CapacityMachinesSlaPolicy sla = getMachinesSlaPolicy(zones);
            machinesEndpoint.recoverStateOnEsmStart(sla);
        }
        machinesEndpoint.recoveredStateOnEsmStart(getProcessingUnit());
    }

    @Override
    protected boolean isRecoveredStateOnEsmStart(ProcessingUnit otherPu) {
        return machinesEndpoint.isRecoveredStateOnEsmStart(otherPu);
    }    

}
