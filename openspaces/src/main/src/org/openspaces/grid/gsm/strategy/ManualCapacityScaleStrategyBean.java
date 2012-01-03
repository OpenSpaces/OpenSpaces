package org.openspaces.grid.gsm.strategy;

import java.util.Map;

import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
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
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaPolicy;
import org.openspaces.grid.gsm.rebalancing.exceptions.RebalancingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;
import org.openspaces.grid.gsm.strategy.ProvisionedMachinesCache.AgentsNotYetDiscoveredException;

public class ManualCapacityScaleStrategyBean extends AbstractScaleStrategyBean 
    implements RebalancingSlaEnforcementEndpointAware , 
               ContainersSlaEnforcementEndpointAware, 
               MachinesSlaEnforcementEndpointAware,
               GridServiceContainerConfigAware {

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
                
        int targetNumberOfContainers = calcTargetNumberOfContainers();
        
        memoryInMB = targetNumberOfContainers * containersConfig.getMaximumMemoryCapacityInMB();
    }

    private int calcTargetNumberOfContainers() {
        if (slaConfig.getMemoryCapacityInMB() > 0) {
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
        int requiredNumberOfContainers = (int)Math.ceil(1.0 * slaConfig.getMemoryCapacityInMB() / containersConfig.getMaximumMemoryCapacityInMB());
        int targetNumberOfContainers = Math.max(
                getMinimumNumberOfMachines(),
                requiredNumberOfContainers);
        
        getLogger().info(
                "targetNumberOfContainers= "+
                "max(minimumNumberOfMachines, ceil(memory/jvm-size))= "+
                "max("+ 
                    getMinimumNumberOfMachines() + "," +
                    "ceil("+
                        slaConfig.getMemoryCapacityInMB() + "/" +
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

    @Override
    public void enforceSla() throws SlaEnforcementInProgressException {
        
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

   
    private int calcTargetNumberOfContainersForPartitionedSchema() {
        
        double totalNumberOfInstances = getProcessingUnit().getTotalNumberOfInstances();
        double avgInstanceCapacityInMB = slaConfig.getMemoryCapacityInMB()/totalNumberOfInstances;
        getLogger().info(
                "instanceCapacityInMB= "+
                "memoryCapacityInMB/(numberOfInstances*(1+numberOfBackups))= "+
                slaConfig.getMemoryCapacityInMB()+"/"+totalNumberOfInstances+"= " +
                avgInstanceCapacityInMB);
        
        double containerCapacityInMB = containersConfig.getMaximumMemoryCapacityInMB();
        
        if (containerCapacityInMB < avgInstanceCapacityInMB) {
            throw new BeanConfigurationException(
                    "Reduce total capacity from " + slaConfig.getMemoryCapacityInMB() +"MB to " + containerCapacityInMB*totalNumberOfInstances+"MB. " +
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
        
        int targetNumberOfContainers = (int)Math.ceil(1.0 *slaConfig.getMemoryCapacityInMB() / containerCapacityInMB);
        getLogger().info(
                "targetNumberOfContainers= "+
                "ceil(memoryCapacity/containerCapacityInMB)= "+
                "ceil("+slaConfig.getMemoryCapacityInMB() +"/"+ containerCapacityInMB+") =" +
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


    private void enforceMachinesSla() 
            throws AgentsNotYetDiscoveredException, 
                   MachinesSlaEnforcementInProgressException , GridServiceAgentSlaEnforcementInProgressException{
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing machines SLA.");
        }
        
        final CapacityMachinesSlaPolicy sla = new CapacityMachinesSlaPolicy();
        sla.setMachineProvisioning(super.getMachineProvisioning());
        CapacityRequirements capacityRequirements = new CapacityRequirements(
                new CpuCapacityRequirement(slaConfig.getNumberOfCpuCores()),
                new MemoryCapacityRequirement(memoryInMB));
        Map<String, Long> drivesCapacityInMB = slaConfig.getDrivesCapacityInMB();
        for (String drive : drivesCapacityInMB.keySet()) {
            capacityRequirements = capacityRequirements.add(
                    new DriveCapacityRequirement(drive,drivesCapacityInMB.get(drive)));
        }
        sla.setCapacityRequirements(capacityRequirements);
        sla.setMinimumNumberOfMachines(getMinimumNumberOfMachines());
        sla.setMaximumNumberOfMachines(getMaximumNumberOfInstances());
        sla.setMaximumNumberOfContainersPerMachine(getMaximumNumberOfContainersPerMachine());
        sla.setContainerMemoryCapacityInMB(containersConfig.getMaximumMemoryCapacityInMB());
        sla.setMachineIsolation(getIsolation());
        
        try {
            sla.setProvisionedAgents(getDiscoveredAgents());
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
        sla.setMaximumNumberOfConcurrentRelocationsPerMachine(slaConfig.getMaxConcurrentRelocationsPerMachine());
        sla.setSchemaConfig(getSchemaConfig());
        sla.setAllocatedCapacity(machinesEndpoint.getAllocatedCapacity());
        try {
            rebalancingEndpoint.enforceSla(sla);
            puInstanceProvisioningCompletedEvent();

        }
        catch (RebalancingSlaEnforcementInProgressException e) {
            puInstanceProvisioningInProgressEvent(e);
            throw e;
        }
    }

    public ManualCapacityScaleConfig getConfig() {
        return slaConfig;
    }
    
    private int getMaximumNumberOfContainersPerMachine() {
        return slaConfig.isAtMostOneContainersPerMachine()?1:getMaximumNumberOfInstances();
    }

    @Override
    protected boolean isUndeploying() {
        return false;
    }
}
