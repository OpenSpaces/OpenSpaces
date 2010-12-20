package org.openspaces.grid.gsm.machines;

import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

public class MachinesSlaPolicy extends ServiceLevelAgreementPolicy {
 
    long memoryInMB;
    double cpu;
    private NonBlockingElasticMachineProvisioning machineProvisioning;
    
    public void setCpu(double cpu) {
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
    
    @Override
    public boolean equals(Object other) {
        return other instanceof MachinesSlaPolicy &&
        ((MachinesSlaPolicy)other).memoryInMB == this.memoryInMB &&
        ((MachinesSlaPolicy)other).cpu == this.cpu;
    }
    

}
