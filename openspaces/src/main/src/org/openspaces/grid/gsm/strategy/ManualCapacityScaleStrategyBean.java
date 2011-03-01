package org.openspaces.grid.gsm.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.grid.gsm.GridServiceContainerConfigAware;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.machines.CapacityMachinesSlaPolicy;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaPolicy;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

public class ManualCapacityScaleStrategyBean extends AbstractScaleStrategyBean 
    implements RebalancingSlaEnforcementEndpointAware , 
               ContainersSlaEnforcementEndpointAware, 
               MachinesSlaEnforcementEndpointAware,
               GridServiceContainerConfigAware {

    private static final String rebalancingAlertGroupUidPrefix = "4499C1ED-1584-4387-90CF-34C5EC236644";
    private static final String containersAlertGroupUidPrefix = "47A94111-5665-4214-9F7A-2962D998DD12";
    private static final String machinesAlertGroupUidPrefix = "3BA87E89-449A-4abc-A632-4732246A9EE4";

    // injected 
    private ManualCapacityScaleConfig slaConfig;
    private MachinesSlaEnforcementEndpoint machinesEndpoint;
    private ContainersSlaEnforcementEndpoint containersEndpoint;
    private RebalancingSlaEnforcementEndpoint rebalancingEndpoint;
    private GridServiceContainerConfig containersConfig;
    
    // created by afterPropertiesSet()
    private long memoryInMB;
    
    
    public void setMachinesSlaEnforcementEndpoint(MachinesSlaEnforcementEndpoint machinesService) {
        this.machinesEndpoint = machinesService;
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
        
        slaConfig = new ManualCapacityScaleConfig(super.getProperties());
                
        int targetNumberOfContainers = 
            slaConfig.getMemoryCapacityInMB() > 0 && getSchemaConfig().isPartitionedSync2BackupSchema() ?
                    calcTargetNumberOfContainersForPartitionedSchema() :
                    calcDefaultTargetNumberOfContainers();
        
        memoryInMB = targetNumberOfContainers * containersConfig.getMaximumJavaHeapSizeInMB();
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

    @Override
    public void run() {
        
        super.run();
        
        try {
            getLogger().debug("Enforcing machines SLA.");
            boolean machinesSlaEnforced = enforceMachinesSla();
            if (getLogger().isDebugEnabled()) {
            
                if (machinesEndpoint.isGridServiceAgentsPendingDeallocation()) {
                    getLogger().debug(
                            "Machines SLA cannot be reached until containers are removed before scale in. "+
                            "Allocated Capacity:" + machinesEndpoint.getAllocatedCapacity() +
                            "Machines: " + 
                                MachinesSlaUtils.machinesToString(
                                        MachinesSlaUtils.convertAgentUidsToAgents(
                                                machinesEndpoint.getAllocatedCapacity().getAgentUids(), 
                                                getAdmin())));
                }
                else if (!machinesSlaEnforced) {
                    getLogger().debug("Machines SLA has not been reached");
                }
            }
            if (machinesSlaEnforced || 
                machinesEndpoint.isGridServiceAgentsPendingDeallocation()) {

                getLogger().debug("Enforcing containers SLA.");
                boolean containersSlaEnforced = enforceContainersSla();
                if (getLogger().isDebugEnabled()) {
                    if (containersEndpoint.isContainersPendingDeallocation()) {
                        getLogger().debug(
                                "Containers SLA cannot be reached until processingunit is relocated before scale in. "+
                                "Approved Containers: " + toString(containersEndpoint.getContainers()));
                    }
                    else if (!containersSlaEnforced) {
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
        catch (AgentsNotYetDiscoveredException e) {
            getLogger().debug("Existing agents not discovered yet",e);
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

    private static String toString(GridServiceContainer[] containers) {
        final List<String> containersToString = new ArrayList<String>();
        for (final GridServiceContainer container : containers) {
            containersToString.add(MachinesSlaUtils.gscToString(container));
        }
        return Arrays.toString(containersToString.toArray(new String[containersToString.size()]));
    }

    
    private int calcTargetNumberOfContainersForPartitionedSchema() {
        
        double totalNumberOfInstances = getProcessingUnit().getTotalNumberOfInstances();
        double instanceCapacityInMB = slaConfig.getMemoryCapacityInMB()/totalNumberOfInstances;
        getLogger().info(
                "instanceCapacityInMB= "+
                "memoryCapacityInMB/(numberOfInstances*(1+numberOfBackups))= "+
                slaConfig.getMemoryCapacityInMB()+"/"+totalNumberOfInstances+"= " +
                instanceCapacityInMB);
        
        double containerCapacityInMB = containersConfig.getMaximumJavaHeapSizeInMB();
        
        if (containerCapacityInMB < instanceCapacityInMB) {
            throw new BeanConfigurationException(
                    "Container capacity (-Xmx) is " + containerCapacityInMB+"MB , "+
                    "given " + totalNumberOfInstances + " instances, the total capacity =" 
                    +containerCapacityInMB+"MB *"+totalNumberOfInstances + "= " + 
                    containerCapacityInMB*totalNumberOfInstances+"MB. "+
                    "Reduce total capacity from " + slaConfig.getMemoryCapacityInMB() +"MB to " + containerCapacityInMB*totalNumberOfInstances+"MB."); 
        }
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
                targetNumberOfContainers);
                
        int numberOfBackups = getProcessingUnit().getNumberOfBackups();
        if (targetNumberOfContainers < numberOfBackups +1) {
         // raise exception if min number of containers conflicts with the specified memory capacity.
            int recommendedMemoryCapacityInMB = (int)((numberOfBackups +1) * containerCapacityInMB);
            throw new BeanConfigurationException(
                    targetNumberOfContainers + " containers are needed in order to scale to " + slaConfig.getMemoryCapacityInMB() + "m , "+
                    "which cannot support " + (numberOfBackups==1?"one backup":numberOfBackups+" backups") + " per partition. "+
                    "Increase the memory capacity to " + recommendedMemoryCapacityInMB +"m");
        }
        
        if (targetNumberOfContainers == 0) {
            throw new IllegalStateException("targetNumberOfContainers cannot be zero");
        }
        return targetNumberOfContainers;
    }


    private boolean enforceMachinesSla() throws AgentsNotYetDiscoveredException {
        final CapacityMachinesSlaPolicy sla = new CapacityMachinesSlaPolicy();
        sla.setMachineProvisioning(super.getMachineProvisioning());
        sla.setCpuCapacity(slaConfig.getNumberOfCpuCores());
        sla.setMemoryCapacityInMB(memoryInMB);
        sla.setMinimumNumberOfMachines(getMinimumNumberOfMachines());
        sla.setMaximumNumberOfMachines(getMaximumNumberOfInstances());
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
                "Total machines memory for " + getProcessingUnit().getName() + " " + 
                "has reached its target of " + memoryInMB + "MB");
        }
        else {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                containersAlertGroupUidPrefix,
                "Machines Capacity SLA",
                "Total machines memory for " + getProcessingUnit().getName() + " " + 
                "is below the target "+ memoryInMB + "MB");
        }
        
        return reachedSla;

    }
    
    private boolean enforceContainersSla() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        
        final ContainersSlaPolicy sla = new ContainersSlaPolicy();
        sla.setNewContainerConfig(containersConfig);
        sla.setAllocatedCapacity(machinesEndpoint.getAllocatedCapacity());
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
                "Contains capacity for " + getProcessingUnit().getName() + " " + 
                "has been reached: " +
                memoryInMB +" MB memory" +
                (slaConfig.getNumberOfCpuCores()>0 ? " and " + slaConfig.getNumberOfCpuCores() +" cpu cores " : "" ));
        }
        else {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                containersAlertGroupUidPrefix,
                "Containers Capacity SLA",
                "Contains capacity for " + getProcessingUnit().getName() + " " + 
                "has not reached its sla. The sla is " + 
                memoryInMB +" MB memory" +
                (slaConfig.getNumberOfCpuCores()>0 ? " and " + slaConfig.getNumberOfCpuCores() +" cpu cores " : ""));
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
}
