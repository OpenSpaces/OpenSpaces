package org.openspaces.admin.pu.elastic.config;

public interface MaxNumberOfContainersPerMachineScaleConfig {

    /**
     * Limits the maximum number of containers per machine to the specified value.
     * For example in the following scenario, given minimum per machine 3,
     * and maximum per machine is 4, a new GSC cannot be deployed until
     * a new machine is added to the machine pool.
     * 
     * Machine A: GSC GSC GSC GSC
     * Machine B: GSC GSC GSC GSC
     */
    public void setMaxNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine);
    
    public int getMaxNumberOfContainersPerMachine();
    
}
