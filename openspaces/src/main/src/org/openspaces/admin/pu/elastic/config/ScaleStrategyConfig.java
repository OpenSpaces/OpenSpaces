package org.openspaces.admin.pu.elastic.config;

import org.openspaces.admin.bean.BeanConfig;

public interface ScaleStrategyConfig extends BeanConfig {

    /**
     * Sets the polling interval in which the scale strategy SLA is monitored and enforced.
     * @param seconds - the polling interval in seconds
     */
    void setPollingIntervalSeconds(int seconds);
    
    int getPollingIntervalSeconds();
    
    int getReservedMemoryCapacityPerMachineInMB();

    /**
     * Sets the expected amount of memory per machine that is reserved for processes other than grid containers.
     * These include Grid Service Manager, Lookup Service or any other daemon running on the system.
     * 
     * Default value is 1024 MB. 
     * 
     * For example, by default, a 16GB server  
     * can run 3 containers 5GB each, since it approximately leaves 1024MB memory free.
     * 
     * This is an advanced property setting.
     * 
     * @param reservedInMB - amount of reserved memory in MB
     */
    void setReservedMemoryCapacityPerMachineInMB(int reservedInMB);
    
    boolean getDedicatedManagementMachines();
    
    /**
     * If specified restricts the {@link org.openspaces.admin.gsc.GridServiceContainer} 
     * from being started on the same machine as the {@link org.openspaces.admin.gsm.GridServiceManager} , {@link org.openspaces.admin.lus.LookupService}
     * 
     * Default value is false
     * 
     * This is an advanced property setting.
     * 
     * @param dedicatedManagementMachines - set to false for a dedicated management machine
     */
    void setDedicatedManagementMachines(boolean dedicatedManagementMachines);
    
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
