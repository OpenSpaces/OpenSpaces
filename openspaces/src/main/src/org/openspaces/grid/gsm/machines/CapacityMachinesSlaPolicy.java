package org.openspaces.grid.gsm.machines;



public class CapacityMachinesSlaPolicy extends AbstractMachinesSlaPolicy {
 
    private long memoryInMB;
    private double cpu;
        
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
    
    @Override
    public boolean equals(Object other) {
        return other instanceof CapacityMachinesSlaPolicy &&
        super.equals(other) &&
        ((CapacityMachinesSlaPolicy)other).memoryInMB == this.memoryInMB &&
        ((CapacityMachinesSlaPolicy)other).cpu == this.cpu;
        //TODO: Should we also compare machineProvisioning ?
    }

    public boolean isStopMachineSupported() {
        return true;
    }

    @Override
    public String getScaleStrategyName() {
        return "Manual Capacity Scale Strategy";
    }

}
