package org.openspaces.grid.gsm.strategy;

import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.grid.gsm.GridServiceContainerConfigAware;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.machines.CapacityMachinesSlaPolicy;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementEndpointDestroyedException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementException;
import org.openspaces.grid.gsm.strategy.ProvisionedMachinesCache.AgentsNotYetDiscoveredException;

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
        super.afterPropertiesSet();
    
        if (machinesEndpoint == null) {
            throw new IllegalStateException("machines endpoint cannot be null.");
        }
        
        if (containersEndpoint == null) {
            throw new IllegalStateException("containers endpoint cannot be null");
        }
    }

    public void enforceSla() throws SlaEnforcementException {
        
        if (!isScaleInProgress()) {
            return;
        }
        
        enforceContainersSla();
        
        enforceMachinesSla();
    }

    private void enforceMachinesSla() throws AgentsNotYetDiscoveredException, SlaEnforcementException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Undeploying machines for " + getProcessingUnit().getName());
        }
        
        final CapacityMachinesSlaPolicy sla = new CapacityMachinesSlaPolicy();
        NonBlockingElasticMachineProvisioning machineProvisioning = super.getMachineProvisioning();
        sla.setMachineProvisioning(machineProvisioning);
        sla.setCapacityRequirements(new CapacityRequirements());
        sla.setMinimumNumberOfMachines(0);
        sla.setMaximumNumberOfMachines(getMaximumNumberOfInstances());
        sla.setMaximumNumberOfContainersPerMachine(getMaximumNumberOfInstances());
        sla.setContainerMemoryCapacityInMB(containersConfig.getMaximumMemoryCapacityInMB());
        sla.setProvisionedAgents(getDiscoveredAgents());
        sla.setMachineIsolation(getIsolation());
        
        try {
            machinesEndpoint.enforceSla(sla);
            resolveMachinesAlert(
                    "Machines for " + getProcessingUnit().getName() + " have been terminated.");
        } catch (SlaEnforcementException e) {
            raiseMachinesAlert(e);
            throw e;
        }
    }

    private void enforceContainersSla() throws SlaEnforcementEndpointDestroyedException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Undeploying containers for " + getProcessingUnit().getName());
        }
        
        final ContainersSlaPolicy sla = new ContainersSlaPolicy();
        sla.setNewContainerConfig(containersConfig);
        sla.setClusterCapacityRequirements(new ClusterCapacityRequirements());
        
        try {
            containersEndpoint.enforceSla(sla);
            resolveContainersAlert(
                    "Containers for " + getProcessingUnit().getName() + " have been terminated.");
        } catch (SlaEnforcementException e) {
            raiseContainersAlert(e);
        }
    }
  

    public ScaleStrategyConfig getConfig() {
        return null;
    }

}
