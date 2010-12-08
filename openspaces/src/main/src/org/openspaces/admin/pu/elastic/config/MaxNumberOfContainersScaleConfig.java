package org.openspaces.admin.pu.elastic.config;

public interface MaxNumberOfContainersScaleConfig {

    /**
     * Defines a maximum for the total number of containers.  
     * 
     * For example in the following scenario, given the maximum is 3,
     * a new container is not started even if a scale out trigger is relevant. 
     * 
     * Machine A: GSC GSC 
     * Machine B: GSC
     * 
     */
    public void setMaxNumberOfContainers(int numberOfContainers);
    
    public int getMaxNumberOfContainers();
}
