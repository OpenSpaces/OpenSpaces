package org.openspaces.admin.pu.elastic.config;

import org.openspaces.admin.bean.BeanConfig;

public interface ScaleStrategyConfig extends BeanConfig {

    /**
     * Sets the polling interval in which the scale strategy SLA is monitored and enforced.
     * @param seconds - the polling interval in seconds
     */
    void setPollingIntervalSeconds(int seconds);
    
    int getPollingIntervalSeconds();
      
    int getMaxConcurrentRelocationsPerMachine();
    
    /**
     * Specifies the number of processing unit instance relocations each machine can handle concurrently.
     * Relocation requires network and CPU resources, and too many concurrent relocations per machine may degrade
     * its performance temporarily. The data recovery running as part of the relocation uses by default 4 threads.
     * So the total number of threads is 4 multiplied by the specified value.
     *  
     * By setting this value higher than 1, processing unit rebalancing
     * completes faster, by using more machine cpu and network resources.
     * 
     * Default value is 1.
     * 
     * This is an advanced property setting.
     * 
     * @param maxNumberOfConcurrentRelocationsPerMachine
     */
    void setMaxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine);
}
