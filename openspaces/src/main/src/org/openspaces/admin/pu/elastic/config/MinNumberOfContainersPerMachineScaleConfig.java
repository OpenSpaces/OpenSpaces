package org.openspaces.admin.pu.elastic.config;

public interface MinNumberOfContainersPerMachineScaleConfig {

    /**
     * Starts at least the specified number of containers per machine.
     * 
     * For example in the following scenario, given minimum per machine is 3,
     * and maximum per machine is 4, 
     * when machine C is started, 3 GSCs will be started on it.
     * 
     * Machine A: GSC GSC GSC GSC
     * Machine B: GSC GSC GSC
     * 
     */
    public void setMinNumberOfContainersPerMachine(int numberOfContainers);
    
    public int getMinNumberOfContainersPerMachine();
}
