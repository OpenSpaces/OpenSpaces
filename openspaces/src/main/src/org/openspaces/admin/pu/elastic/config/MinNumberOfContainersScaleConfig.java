package org.openspaces.admin.pu.elastic.config;

public interface MinNumberOfContainersScaleConfig {

    /**
     * Defines a minimum for the total number of containers.  
     * 
     * For example in the following scenario, given the minimum is 3,
     * existing containers will not be removed even if a relevant 
     * scale trigger is relevant. 
     * 
     * Machine A: GSC GSC 
     * Machine B: GSC
     * 
     */
    public void setMinNumberOfContainers(int minNumberOfContainers);
    
    public int getMinNumberOfContainers();
}
