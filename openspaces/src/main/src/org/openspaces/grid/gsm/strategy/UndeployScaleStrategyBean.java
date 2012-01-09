package org.openspaces.grid.gsm.strategy;

import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
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
import org.openspaces.grid.gsm.machines.exceptions.WaitingForDiscoveredMachinesException;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.rebalancing.exceptions.ElasticProcessingUnitInstanceUndeployInProgress;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

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
        
        enforceMachinesSla();
    }

    private void enforceMachinesSla() throws WaitingForDiscoveredMachinesException, MachinesSlaEnforcementInProgressException, GridServiceAgentSlaEnforcementInProgressException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Undeploying machines for " + getProcessingUnit().getName());
        }
        
        final CapacityMachinesSlaPolicy sla = new UndeployMachinesSlaPolicy(getAdmin());
        NonBlockingElasticMachineProvisioning machineProvisioning = super.getMachineProvisioning();
        sla.setMachineProvisioning(machineProvisioning);
        sla.setMaximumNumberOfMachines(getMaximumNumberOfInstances());
        sla.setMaximumNumberOfContainersPerMachine(getMaximumNumberOfInstances());
        sla.setContainerMemoryCapacityInMB(containersConfig.getMaximumMemoryCapacityInMB());
        sla.setMachineIsolation(getIsolation());
        sla.setDiscoveredMachinesCache(getDiscoveredMachinesCache());
        
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

}
