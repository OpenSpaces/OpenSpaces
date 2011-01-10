package org.openspaces.grid.gsm.machines;

import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

public class MachinesSlaPolicy extends ServiceLevelAgreementPolicy {
 
    private long memoryInMB;
    private double cpu;
    private NonBlockingElasticMachineProvisioning machineProvisioning;
    private int minimumNumberOfMachines;
    private long reservedMemoryCapacityPerMachineInMB;
    private long containerMemoryCapacityInMB;
    private boolean allowDeploymentOnManagementMachine;
    
    public void setCpuCapacity(double cpu) {
        this.cpu = cpu;
    }
    
    public double getCpu() {
        return this.cpu;
    }
    
    public void setMemoryCapacityInMB(long memory) {
        this.memoryInMB = memory;
    }
    
    public long getMemoryCapacityInMB() {
        return this.memoryInMB;
    }
    
    public NonBlockingElasticMachineProvisioning getMachineProvisioning() {
        return this.machineProvisioning;
    }
    
    public void setMachineProvisioning(NonBlockingElasticMachineProvisioning machineProvisioning) {
        this.machineProvisioning = machineProvisioning;
    }
    
    
    public int getMinimumNumberOfMachines() {
        return minimumNumberOfMachines;
    }
    
    public void setMinimumNumberOfMachines(int minimumNumberOfMachines) {
        this.minimumNumberOfMachines = minimumNumberOfMachines;
    }
    
    public long getReservedMemoryCapacityPerMachineInMB() {
        return reservedMemoryCapacityPerMachineInMB;
    }
    
    public void setReservedMemoryCapacityPerMachineInMB(int reservedInMB) {
        this.reservedMemoryCapacityPerMachineInMB =reservedInMB ; 
    }

    public void setContainerMemoryCapacityInMB(long containerMemoryCapacityInMB) {
        this.containerMemoryCapacityInMB = containerMemoryCapacityInMB;       
    }
    
    public long getContainerMemoryCapacityInMB() {
        return this.containerMemoryCapacityInMB;
    }


    public boolean getAllowDeploymentOnManagementMachine() {
        return this.allowDeploymentOnManagementMachine;
    }
    
    public void setAllowDeploymentOnManagementMachine(boolean allowDeploymentOnManagementMachine) {
        this.allowDeploymentOnManagementMachine = allowDeploymentOnManagementMachine;
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof MachinesSlaPolicy &&
        ((MachinesSlaPolicy)other).memoryInMB == this.memoryInMB &&
        ((MachinesSlaPolicy)other).cpu == this.cpu &&
        ((MachinesSlaPolicy)other).reservedMemoryCapacityPerMachineInMB == this.reservedMemoryCapacityPerMachineInMB &&
        ((MachinesSlaPolicy)other).containerMemoryCapacityInMB == this.containerMemoryCapacityInMB &&
        ((MachinesSlaPolicy)other).minimumNumberOfMachines == this.minimumNumberOfMachines &&
        ((MachinesSlaPolicy)other).allowDeploymentOnManagementMachine == this.allowDeploymentOnManagementMachine;
    }

}
