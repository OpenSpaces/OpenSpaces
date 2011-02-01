package org.openspaces.grid.gsm.machines;

import java.util.HashSet;
import java.util.Set;

import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

abstract class AbstractMachinesSlaPolicy extends ServiceLevelAgreementPolicy{

    private int minimumNumberOfMachines;
    private long reservedMemoryCapacityPerMachineInMB;
    private long containerMemoryCapacityInMB;
    private boolean allowDeploymentOnManagementMachine;
    private Set<String> machineZones = new HashSet<String>();
    
    public boolean getAllowDeploymentOnManagementMachine() {
        return this.allowDeploymentOnManagementMachine;
    }
    
    public void setAllowDeploymentOnManagementMachine(boolean allowDeploymentOnManagementMachine) {
        this.allowDeploymentOnManagementMachine = allowDeploymentOnManagementMachine;
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

    public void setMachineZones(Set<String> machineZones) {
        this.machineZones = machineZones;
    }
    
    public Set<String> getMachineZones() {
        return this.machineZones;
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof AbstractMachinesSlaPolicy &&
        ((AbstractMachinesSlaPolicy)other).reservedMemoryCapacityPerMachineInMB == this.reservedMemoryCapacityPerMachineInMB &&
        ((AbstractMachinesSlaPolicy)other).containerMemoryCapacityInMB == this.containerMemoryCapacityInMB &&
        ((AbstractMachinesSlaPolicy)other).minimumNumberOfMachines == this.minimumNumberOfMachines &&
        ((AbstractMachinesSlaPolicy)other).allowDeploymentOnManagementMachine == this.allowDeploymentOnManagementMachine &&
        ((AbstractMachinesSlaPolicy)other).machineZones.equals(machineZones);
    }
}
