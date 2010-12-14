package org.openspaces.grid.gsm.machines;

import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

public class MachinesSlaPolicy extends ServiceLevelAgreementPolicy {
 
    int memory;
    double cpu;
    
    public void setCpu(double cpu) {
        this.cpu = cpu;
    }
    
    public double getCpu() {
        return this.cpu;
    }
    
    public void setMemoryCapacityInMB(int memory) {
        this.memory = memory;
    }
    
    public int getMemoryCapacityInMB() {
        return this.memory;
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof MachinesSlaPolicy &&
        ((MachinesSlaPolicy)other).memory == this.memory &&
        ((MachinesSlaPolicy)other).cpu == this.cpu;
    }
    

}
