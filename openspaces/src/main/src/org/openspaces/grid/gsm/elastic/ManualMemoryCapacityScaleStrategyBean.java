package org.openspaces.grid.gsm.elastic;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.alerts.AlertFactory;
import org.openspaces.admin.alerts.AlertSeverity;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.ManualMemoryCapacityScaleConfig;
import org.openspaces.grid.gsm.ProcessingUnitAware;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.MachinesSlaPolicy;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaPolicy;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

public class ManualMemoryCapacityScaleStrategyBean 
    implements ScaleStrategyBean, 
               RebalancingSlaEnforcementEndpointAware , 
               ContainersSlaEnforcementEndpointAware, 
               MachinesSlaEnforcementEndpointAware,
               ProcessingUnitAware, 
               Runnable {

    private static final Log logger = LogFactory.getLog(ManualMemoryCapacityScaleStrategyBean.class);
    private static final String rebalancingAlertGroupUidPrefix = "4499C1ED-1584-4387-90CF-34C5EC236644";
    private static final String containersAlertGroupUidPrefix = "47A94111-5665-4214-9F7A-2962D998DD12";
    private static final String machinesAlertGroupUidPrefix = "3BA87E89-449A-4abc-A632-4732246A9EE4";

    // injected 
    private InternalAdmin admin;
    private ManualMemoryCapacityScaleConfig config;
    private MachinesSlaEnforcementEndpoint machinesService;
    private ContainersSlaEnforcementEndpoint containersService;
    private RebalancingSlaEnforcementEndpoint rebalancingService;
    private ProcessingUnit pu;
    
    // created by afterPropertiesSet()
    @SuppressWarnings("unchecked")
    private Future scheduledTask;
    private int targetNumberOfContainers;

    public Map<String, String> getProperties() {
        return config.getProperties();
    }

    public void setProcessingUnit(ProcessingUnit pu) {
        this.pu = pu;
    }
    
    public void setAdmin(Admin admin) {
        this.admin = (InternalAdmin) admin;
    }

    public void setMachinesSlaEnforcementEndpoint(MachinesSlaEnforcementEndpoint machinesService) {
        this.machinesService = machinesService;
    }

    public void setContainersSlaEnforcementEndpoint(ContainersSlaEnforcementEndpoint containersService) {
        this.containersService = containersService;
    }
    
    public void setRebalancingSlaEnforcementEndpoint(RebalancingSlaEnforcementEndpoint relocationService) {
        this.rebalancingService = relocationService;
    }

    public void afterPropertiesSet() {
        
        if (pu.getSpace() == null) {
            throw new BeanConfigurationException("Processing Unit " + pu.getName() + " cannot scale by memory capacity, since it is not stateful and not a datagrid. Choose a different scale algorithm.");
        }
        
        int numberOfBackups = pu.getNumberOfBackups();
       
        if (config.getMinNumberOfContainers() != 0 &&
            config.getMinNumberOfContainers() <  1 + numberOfBackups) {
            throw new BeanConfigurationException(
                    "Minimum number of containers " + config.getMinNumberOfContainers() + " " + 
                    "cannot support " + (numberOfBackups==1?"one backup":numberOfBackups+" backups") + " per partition. "+
                    "Either don't use the minimum number of containers property or set it to " + (numberOfBackups+1));
        }

        this.targetNumberOfContainers = calcTargetNumberOfContainers();
        
        scheduledTask = 
        ((InternalAdmin)admin).scheduleWithFixedDelayNonBlockingStateChange(
        this, 0L, (long)config.getPollingIntervalSeconds(), TimeUnit.SECONDS);
    }

    public void destroy() {
        
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }

    public void setProperties(Map<String, String> properties) {
        config = new ManualMemoryCapacityScaleConfig();
        config.setProperties(properties);
    }

    public void run() {
        try {
            
            boolean machinesSlaEnforced = enforceMachinesSla();
            
            if (machinesSlaEnforced || 
                machinesService.getGridServiceAgentsPendingShutdown().length >0) {

                boolean containersSlaEnforced = enforceContainersSla();

                if (containersSlaEnforced || 
                    containersService.getContainersPendingShutdown().length > 0) {

                    enforceRebalancingSla(containersService.getContainers());
                }
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

    private int calcTargetNumberOfContainers() {
        
        if (config.getMemoryCapacityInMB() <= 0) {
            throw new BeanConfigurationException("The specified memory capacity " + config.getMemoryCapacityInMB() + "m cannot be negative or zero.");
        }
        
        double totalNumberOfInstances = pu.getTotalNumberOfInstances();
        double instanceCapacityInMB = config.getMemoryCapacityInMB()/totalNumberOfInstances;
        double containerCapacityInMB = pu.getElasticConfig().getGridServiceContainerConfig().getMaximumJavaHeapSizeInMB();
        double maxNumberOfInstancesPerContainer = Math.floor(containerCapacityInMB / instanceCapacityInMB); 
        double targetNumberOfContainers = Math.ceil((totalNumberOfInstances)/ maxNumberOfInstancesPerContainer);
        
        int minNumberOfContainers = config.getMinNumberOfContainers();
        if (minNumberOfContainers != 0 && targetNumberOfContainers < minNumberOfContainers) {
                
            // raise exception if min number of containers conflicts with the specified memory capacity.
            int recommendedMemoryCapacityInMB = (int)(minNumberOfContainers * containerCapacityInMB);
            throw new BeanConfigurationException(
                    targetNumberOfContainers + " containers are needed in order to scale to " + config.getMemoryCapacityInMB() + "m . "+
                    "The minimum number of containers is set to " + minNumberOfContainers + ". "+
                    "Either decrease the minimum number of containers to " + targetNumberOfContainers +" or " +
                    "increase the memory capacity to " + recommendedMemoryCapacityInMB +"m");
        }
                
        int numberOfBackups = pu.getNumberOfBackups();
        if (targetNumberOfContainers < numberOfBackups +1) {
         // raise exception if min number of containers conflicts with the specified memory capacity.
            int recommendedMemoryCapacityInMB = (int)((numberOfBackups +1) * containerCapacityInMB);
            throw new BeanConfigurationException(
                    targetNumberOfContainers + " containers are needed in order to scale to " + config.getMemoryCapacityInMB() + "m , "+
                    "which cannot support " + (numberOfBackups==1?"one backup":numberOfBackups+" backups") + " per partition. "+
                    "Increase the memory capacity to " + recommendedMemoryCapacityInMB +"m");
        }
        
        int maxNumberOfContainers = config.getMaxNumberOfContainers();
        if (maxNumberOfContainers != 0 && targetNumberOfContainers > maxNumberOfContainers) {
            
            // raise exception if max number of containers conflicts with the specified memory capacity.
            double recommendedMaxNumberOfInstancesPerContainer = Math.ceil(totalNumberOfInstances/maxNumberOfContainers);
            double recommendedInstanceCapacityInMB = containerCapacityInMB / recommendedMaxNumberOfInstancesPerContainer;
            int recommendedMemoryCapacityInMB = (int)Math.floor(recommendedInstanceCapacityInMB * totalNumberOfInstances);
            if (recommendedMemoryCapacityInMB >= config.getMemoryCapacityInMB()) {
                throw new IllegalStateException("recommended capacity is bigger than specified capacity. recommendedMemoryCapacityInMB (="+recommendedInstanceCapacityInMB+") >= " + config.getMemoryCapacityInMB());
            }
                
            throw new BeanConfigurationException(
                    targetNumberOfContainers + " containers are needed in order to scale to " + config.getMemoryCapacityInMB() + "m . "+
                    "The maximum number of containers is set to " + maxNumberOfContainers + ". "+
                    "Either increase the maximum number of containers to " + targetNumberOfContainers +" or " +
                    "decrease the memory capacity to " + recommendedMemoryCapacityInMB +"m");
        }
        
        return (int) targetNumberOfContainers;
    }


    private boolean enforceMachinesSla() {
        final MachinesSlaPolicy sla = new MachinesSlaPolicy();
        //TODO: Inject MachineProvisioning
        
        return true;
    }
    
    private boolean enforceContainersSla() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        
        final ContainersSlaPolicy sla = new ContainersSlaPolicy();
        sla.setTargetNumberOfContainers(targetNumberOfContainers);
        sla.setNewContainerConfig(pu.getElasticConfig().getGridServiceContainerConfig());
        sla.setGridServiceAgents(machinesService.getGridServiceAgents());
        
        boolean reachedSla = containersService.enforceSla(sla);
        
        if (reachedSla) {
            fireAlert(
                AlertSeverity.OK,
                containersAlertGroupUidPrefix,
                "Target number of containers for " + pu.getName() + " " + 
                "has been reached: " + targetNumberOfContainers);
        }
        else {
            fireAlert(
                AlertSeverity.WARNING, 
                containersAlertGroupUidPrefix,
                "Target number of containers for " + pu.getName() + " " + 
                "is " + targetNumberOfContainers + ". " +
                "Current number of containers is " + 
                containersService.getContainers().length);
        }
        
        return reachedSla;
    }
    
    private boolean enforceRebalancingSla(GridServiceContainer[] containers) 
        throws ServiceLevelAgreementEnforcementEndpointDestroyedException 
    {
        RebalancingSlaPolicy sla = new RebalancingSlaPolicy();
        sla.setContainers(containers);
        
        boolean slaEnforced = rebalancingService.enforceSla(sla);
        
        if (slaEnforced) {
            fireAlert(
                AlertSeverity.OK,
                rebalancingAlertGroupUidPrefix,
                "Rebalancing of " + pu.getName() + " is complete.");
        }
        else {
            fireAlert(
                AlertSeverity.WARNING, 
                rebalancingAlertGroupUidPrefix,
                "Rebalancing of " + pu.getName() + " is in progress.");
        }
        
        return slaEnforced;
    }

    private void fireAlert(AlertSeverity severity,String alertGroupUidPrefix, String alertDescription) {
        AlertFactory alertFactory = new AlertFactory();
        alertFactory.description(alertDescription);
        alertFactory.severity(severity);        
        alertFactory.componentUid(pu.getName());
        alertFactory.groupUid(alertGroupUidPrefix + "-" + pu.getName());
        admin.getAlertManager().fireAlert(alertFactory.toAlert());
        logger.debug(alertDescription);
    }
}
