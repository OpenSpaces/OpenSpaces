package org.openspaces.grid.gsm.strategy;

import org.apache.commons.logging.Log;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.grid.gsm.GridServiceContainerConfigAware;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.machines.CapacityMachinesSlaPolicy;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

public class UndeployScaleStrategyBean extends AbstractScaleStrategyBean

    implements ContainersSlaEnforcementEndpointAware, 
               MachinesSlaEnforcementEndpointAware,
               GridServiceContainerConfigAware {

    private static final String containersAlertGroupUidPrefix = "47A94111-5665-4214-9F7A-2962D998DD12";
    private static final String machinesAlertGroupUidPrefix = "3BA87E89-449A-4abc-A632-4732246A9EE4";

    // injected 
    private MachinesSlaEnforcementEndpoint machinesEndpoint;
    private ContainersSlaEnforcementEndpoint containersEndpoint;
    private GridServiceContainerConfig containersConfig;

    // created by afterPropertiesSet()
    private Log logger;
    private boolean undeployComplete;
    
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

    public void run() {
        
        if (undeployComplete) {
            return;
        }
        
        super.run();
        
        logger.debug("Undeploying processing unit " + getProcessingUnit().getName());
        
        try {
            
            logger.debug("Undeploying containers for " + getProcessingUnit().getName());
            boolean containersSlaEnforced = enforceContainersSla();
            if (logger.isDebugEnabled()) {
                if (!containersSlaEnforced) {
                    logger.debug("Containers undeploy is incomplete.");
                }
            }
            logger.debug("Undeploying machines for " + getProcessingUnit().getName());
            boolean machinesSlaEnforced = enforceMachinesSla();
            if (logger.isDebugEnabled()) {
                if (!machinesSlaEnforced) {
                    logger.debug("Machines undeploy incomplete.");
                }
            }
            
            if (containersSlaEnforced && machinesSlaEnforced) {
                logger.info(getProcessingUnit().getName() + " undeploy is complete.");
                undeployComplete = true;
            }
        }        
        catch (ServiceLevelAgreementEnforcementEndpointDestroyedException e) {
            logger.debug("AdminService was destroyed",e);
        }
        catch (AdminException e) {
            logger.warn("Unhandled AdminException",e);
        }
        catch (Exception e) {
            logger.error("Unhandled Exception",e);
        }
        
    }

    public boolean isUndeployComplete() {
        return undeployComplete;
    }
    
    private boolean enforceMachinesSla() {
        final CapacityMachinesSlaPolicy sla = new CapacityMachinesSlaPolicy();
        NonBlockingElasticMachineProvisioning machineProvisioning = super.getMachineProvisioning();
        sla.setMachineProvisioning(machineProvisioning);
        sla.setCpuCapacity(0);
        sla.setMemoryCapacityInMB(0);
        sla.setMinimumNumberOfMachines(0);
        sla.setReservedMemoryCapacityPerMachineInMB(super.getReservedMemoryCapacityPerMachineInMB());
        sla.setContainerMemoryCapacityInMB(containersConfig.getMaximumJavaHeapSizeInMB());
        sla.setProvisionedAgents(getDiscoveredAgents());
        sla.setMachineIsolation(getIsolation());
        boolean reachedSla = machinesEndpoint.enforceSla(sla);
        
        if (reachedSla) {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                machinesAlertGroupUidPrefix,
                "Machines Capacity SLA",
                "Machines for " + getProcessingUnit().getName() + " have been terminated.");
        }
        else {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                containersAlertGroupUidPrefix,
                "Machines Capacity SLA",
                "Machines for " + getProcessingUnit().getName() + " are being terminated.");
        }
        
        return reachedSla;

    }

    private boolean enforceContainersSla() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        
        final ContainersSlaPolicy sla = new ContainersSlaPolicy();
        sla.setNewContainerConfig(containersConfig);
        sla.setAllocatedCapacity(new AggregatedAllocatedCapacity());
        
        boolean reachedSla = containersEndpoint.enforceSla(sla);
        
        if (reachedSla) {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                containersAlertGroupUidPrefix,
                "Containers Capacity SLA",
                "Containers for " + getProcessingUnit().getName() + " have been terminated.");
        }
        else {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                containersAlertGroupUidPrefix,
                "Containers Capacity SLA",
                "Contains capacity for " + getProcessingUnit().getName() + " " + 
                "Containers for " + getProcessingUnit().getName() + " are being terminated.");
        }
        
        return reachedSla;
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
        logger.debug(alertDescription);
    }

}
