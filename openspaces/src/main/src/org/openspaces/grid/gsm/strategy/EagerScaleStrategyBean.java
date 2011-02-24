package org.openspaces.grid.gsm.strategy;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.grid.gsm.GridServiceContainerConfigAware;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.machines.EagerMachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.EagerMachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.EagerMachinesSlaPolicy;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaPolicy;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

public class EagerScaleStrategyBean extends AbstractScaleStrategyBean 

    implements RebalancingSlaEnforcementEndpointAware , 
               ContainersSlaEnforcementEndpointAware, 
               EagerMachinesSlaEnforcementEndpointAware,
               GridServiceContainerConfigAware {

    private static final String rebalancingAlertGroupUidPrefix = "4499C1ED-1584-4387-90CF-34C5EC236644";
    private static final String containersAlertGroupUidPrefix = "47A94111-5665-4214-9F7A-2962D998DD12";
    private static final String machinesAlertGroupUidPrefix = "3BA87E89-449A-4abc-A632-4732246A9EE4";
    
    // injected 
    private EagerScaleConfig slaConfig;
    private EagerMachinesSlaEnforcementEndpoint machinesEndpoint;
    private ContainersSlaEnforcementEndpoint containersEndpoint;
    private RebalancingSlaEnforcementEndpoint rebalancingEndpoint;
    private GridServiceContainerConfig containersConfig;

    public void setEagerMachinesSlaEnforcementEndpoint(EagerMachinesSlaEnforcementEndpoint endpoint) {
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
    public void run() {
        super.run();
        
        try {
            getLogger().debug("Enforcing machines SLA.");
            boolean machinesSlaEnforced = enforceMachinesSla();
            if (getLogger().isDebugEnabled()) {
                if (!machinesSlaEnforced) {
                    getLogger().debug("Machines SLA has not been reached");
                }
            }
            if (machinesSlaEnforced ||
                machinesEndpoint.isGridServiceAgentsPendingDeallocation()) { 

                getLogger().debug("Enforcing containers SLA.");
                boolean containersSlaEnforced = enforceContainersSla();
                if (getLogger().isDebugEnabled()) {
                    if (!containersSlaEnforced) {
                        getLogger().debug("Containers SLA has not been reached");
                    }
                }
                
                if (containersSlaEnforced || 
                    containersEndpoint.isContainersPendingDeallocation()) {
                    getLogger().debug("Enforcing rebalancing SLA.");
                    boolean rebalancingSlaEnforced = enforceRebalancingSla(containersEndpoint.getContainers());
                    if (getLogger().isDebugEnabled()) {
                        if (!rebalancingSlaEnforced) {
                            getLogger().debug("Rebalancing SLA has not been reached");
                        }
                    }
                }
            }
        }
        catch (ServiceLevelAgreementEnforcementEndpointDestroyedException e) {
            getLogger().debug("AdminService was destroyed",e);
        }
        catch (AdminException e) {
            getLogger().warn("Unhandled AdminException",e);
        }
        catch (Exception e) {
            getLogger().error("Unhandled Exception",e);
        }
        
    }

    private boolean enforceMachinesSla() {
        
        final EagerMachinesSlaPolicy sla = getEagerMachinesSlaPolicy();

        boolean reachedSla = machinesEndpoint.enforceSla(sla);
        
        if (reachedSla) {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                machinesAlertGroupUidPrefix,
                "Machines Capacity SLA",
                "Machines Eager SLA has been reached. Using " + 
                machinesEndpoint.getAllocatedCapacity().getAgentUids().size() + " machines");
        }
        else {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                containersAlertGroupUidPrefix,
                "Machines Capacity SLA",
                "Machines Eager SLA has not reached its target");
        }
        
        return reachedSla;

    }

    private boolean enforceContainersSla() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        
        AggregatedAllocatedCapacity allocatedCapacity = machinesEndpoint.getAllocatedCapacity();
        
        final ContainersSlaPolicy sla = new ContainersSlaPolicy();
        sla.setNewContainerConfig(containersConfig);
        sla.setAllocatedCapacity(allocatedCapacity);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Containers Eager SLA Policy: "+
                    "#gridServiceAgents=" + sla.getAllocatedCapacity().getAgentUids().size() + " "+
                    "newContainerConfig.maximumJavaHeapSizeInMB="+sla.getNewContainerConfig().getMaximumJavaHeapSizeInMB());
        }
        boolean reachedSla = containersEndpoint.enforceSla(sla);
        
        if (reachedSla) {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                containersAlertGroupUidPrefix,
                "Containers Capacity SLA",
                "Eager contains capacity for " + getProcessingUnit().getName() + " has been reached");
        }
        else {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                containersAlertGroupUidPrefix,
                "Containers Capacity SLA",
                "Contains capacity for " + super.getProcessingUnit().getName() + " " + 
                "Eager contains capacity for " + getProcessingUnit().getName() + " has not been reached yet.");
        }
        
        return reachedSla;
    }
    
    private boolean enforceRebalancingSla(GridServiceContainer[] containers) 
        throws ServiceLevelAgreementEnforcementEndpointDestroyedException 
    {
        RebalancingSlaPolicy sla = new RebalancingSlaPolicy();
        sla.setContainers(containers);
        sla.setMaximumNumberOfConcurrentRelocationsPerMachine(slaConfig.getMaxConcurrentRelocationsPerMachine());
        sla.setSchemaConfig(getSchemaConfig());
        sla.setAllocatedCapacity(machinesEndpoint.getAllocatedCapacity());
        boolean slaEnforced = rebalancingEndpoint.enforceSla(sla);
        
        if (slaEnforced) {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                rebalancingAlertGroupUidPrefix,
                "Processing Unit Rebalancing SLA",
                "Rebalancing of " + getProcessingUnit().getName() + " is complete.");
        }
        else {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                rebalancingAlertGroupUidPrefix,
                "Processing Unit Rebalancing SLA",
                "Rebalancing of " + getProcessingUnit().getName() + " is in progress.");
        }
        
        return slaEnforced;
    }

    private void triggerAlert(AlertSeverity severity, AlertStatus status, String alertGroupUidPrefix, String alertName, String alertDescription) {
        AlertFactory alertFactory = new AlertFactory();
        alertFactory.name(alertName);
        alertFactory.description(alertDescription);
        alertFactory.severity(severity);    
        alertFactory.status(status);
        alertFactory.componentUid(getProcessingUnit().getName());
        alertFactory.groupUid(alertGroupUidPrefix + "-" + getProcessingUnit().getName());
        getAdmin().getAlertManager().triggerAlert(alertFactory.toAlert());
        getLogger().debug(alertDescription);
    }
    

    
    private EagerMachinesSlaPolicy getEagerMachinesSlaPolicy() {
        final EagerMachinesSlaPolicy sla = new EagerMachinesSlaPolicy();      
        sla.setMaximumNumberOfMachines(getMaximumNumberOfInstances());
        sla.setMinimumNumberOfMachines(getMinimumNumberOfMachines());
        sla.setReservedMemoryCapacityPerMachineInMB(super.getReservedMemoryCapacityPerMachineInMB());
        sla.setContainerMemoryCapacityInMB(containersConfig.getMaximumJavaHeapSizeInMB());
        sla.setProvisionedAgents(getDiscoveredAgents());
        sla.setMachineIsolation(getIsolation());
        return sla;
    }

}
