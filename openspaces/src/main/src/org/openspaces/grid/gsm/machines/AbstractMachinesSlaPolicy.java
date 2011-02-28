package org.openspaces.grid.gsm.machines;

import java.util.Collection;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

public abstract class AbstractMachinesSlaPolicy extends ServiceLevelAgreementPolicy{

    private int maxNumberOfMachines;
    private int minimumNumberOfMachines;
    private long containerMemoryCapacityInMB;
    private NonBlockingElasticMachineProvisioning machineProvisioning;
    private ElasticProcessingUnitMachineIsolation machineIsolation;
    
    private Collection<GridServiceAgent> agents;
    
    public int getMinimumNumberOfMachines() {
        return minimumNumberOfMachines;
    }
    
    public void setMinimumNumberOfMachines(int minimumNumberOfMachines) {
        this.minimumNumberOfMachines = minimumNumberOfMachines;
    }
    
    public long getReservedMemoryCapacityPerMachineInMB() {
        return machineProvisioning.getConfig().getReservedMemoryCapacityPerMachineInMB();
    }
    
    public void setContainerMemoryCapacityInMB(long containerMemoryCapacityInMB) {
        this.containerMemoryCapacityInMB = containerMemoryCapacityInMB;       
    }
    
    public long getContainerMemoryCapacityInMB() {
        return this.containerMemoryCapacityInMB;
    }

    public Collection<GridServiceAgent> getProvisionedAgents() {
        return this.agents;
    }
    
    public void setProvisionedAgents(Collection<GridServiceAgent> agents) {
        this.agents = agents;
    }
    

    /* Optional Argument */
    public NonBlockingElasticMachineProvisioning getMachineProvisioning() {
        return this.machineProvisioning;
    }
    
    public void setMachineProvisioning(NonBlockingElasticMachineProvisioning machineProvisioning) {
        this.machineProvisioning = machineProvisioning;
    }

    public void setMaximumNumberOfMachines(int maxNumberOfMachines) {
        this.maxNumberOfMachines = maxNumberOfMachines;
    }
    
    public int getMaximumNumberOfMachines() {
        return this.maxNumberOfMachines;
    }

    public ElasticProcessingUnitMachineIsolation getMachineIsolation() {
        return machineIsolation;
    }
    
    public void setMachineIsolation(ElasticProcessingUnitMachineIsolation isolation) {
        this.machineIsolation = isolation;
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof AbstractMachinesSlaPolicy &&
        ((AbstractMachinesSlaPolicy)other).getReservedMemoryCapacityPerMachineInMB() == this.getReservedMemoryCapacityPerMachineInMB() &&
        ((AbstractMachinesSlaPolicy)other).containerMemoryCapacityInMB == this.containerMemoryCapacityInMB &&
        ((AbstractMachinesSlaPolicy)other).minimumNumberOfMachines == this.minimumNumberOfMachines &&
        ((AbstractMachinesSlaPolicy)other).machineIsolation.equals(this.machineIsolation) &&
        ((AbstractMachinesSlaPolicy)other).maxNumberOfMachines == maxNumberOfMachines &&
        ((AbstractMachinesSlaPolicy)other).isStopMachineSupported() == isStopMachineSupported() &&
        ((AbstractMachinesSlaPolicy)other).agents.equals(agents);
    }

    public abstract boolean isStopMachineSupported();

    public abstract String getScaleStrategyName();
}
