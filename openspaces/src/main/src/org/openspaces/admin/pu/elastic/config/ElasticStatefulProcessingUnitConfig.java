/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.admin.pu.elastic.config;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.internal.pu.elastic.config.AbstractElasticProcessingUnitConfig;
import org.openspaces.admin.pu.config.ProcessingUnitConfig;
import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigFactory;

/**
 * @author itaif
 * @since 9.0.1
 */
public class ElasticStatefulProcessingUnitConfig 
    extends AbstractElasticProcessingUnitConfig 
    implements ProcessingUnitConfigFactory{

    public static final String MAX_MEMORY_CAPACITY_MEGABYTES_DYNAMIC_PROPERTY = "max-memory-capacity-megabytes";
    public static final String MIN_MEMORY_CAPACITY_MEGABYTES_DYNAMIC_PROPERTY = "min-memory-capacity-megabytes";
    
    Map<String,String> scaleStrategy;
    private long maxMemoryCapacityInMB;
    private int numberOfBackupInstancesPerPartition = 1;
    private int numberOfPartitions;
    private int maxProcessingUnitInstancesFromSamePartitionPerMachine = 1;
    private double maxNumberOfCpuCores;
    private double minNumberOfCpuCoresPerMachine;

    public ElasticStatefulProcessingUnitConfig() {
        super();
        
     // add an elastic property indicating the cluster schema partitioned-sync2backup
        new ProcessingUnitSchemaConfig(super.getElasticProperties()).setPartitionedSync2BackupSchema();        

        // this default context property ensures that during active-election (primary relocations) 
        // the proxy keeps retrying 20 times.
        // see also the wiki documentation on the active election property: cluster-config.groups.group.fail-over-policy.active-election.yield-time
        super.addContextPropertyDefault("space-config.proxy-settings.connection-retries","20");
    }
    
    @Override
    public ProcessingUnitConfig toProcessingUnitConfig(Admin admin) {
      
        ProcessingUnitConfig config = super.toProcessingUnitConfig();

        if (getMachineProvisioning() != null && getMinNumberOfCpuCoresPerMachine() <= 0) {
            // try to figure out from machine provisioning
            setMinNumberOfCpuCoresPerMachine(super.getMachineProvisioning().getMinimumNumberOfCpuCoresPerMachine());
            if (getMinNumberOfCpuCoresPerMachine() <= 0 &&
                !(getMachineProvisioning() instanceof DiscoveredMachineProvisioningConfig)) {
                
                throw new IllegalStateException("Elastic Machine Provisioning configuration must supply the expected minimum number of CPU cores per machine.");
            }
        }
        
        
        if (this.getMaxMemoryCapacityInMB() == 0 && this.getNumberOfPartitions() == 0) {
            throw new IllegalStateException("maxMemoryCapacity must be defined.");
        }
        
        if (this.getMaxMemoryCapacityInMB() != 0 && this.getNumberOfPartitions() != 0) {
            throw new IllegalStateException("numberOfPartitions conflicts with maxMemoryCapacity. Please specify only one of these properties.");
        }
        
        if (this.getMaxNumberOfCpuCores() != 0 && this.getNumberOfPartitions() != 0) {
            throw new IllegalStateException("numberOfPartitions conflicts with maxNumberOfCpuCores. Please specify only one of these properties.");
        }

        int numberOfInstances = this.getNumberOfPartitions();
        if (numberOfInstances == 0) {
            numberOfInstances = Math.max(calcNumberOfPartitionsFromMemoryRequirements(),calcNumberOfPartitionsFromCpuRequirements(admin));
        }
        
        if (getNumberOfBackupInstancesPerPartition() == 0) {
            // allow instances from DIFFERENT partitions to deploy on same Container
            config.setMaxInstancesPerMachine(0);   
            config.setMaxInstancesPerVM(0);
        }
        else {
            // disallow instances from SAME partition to deploy on same Container
            config.setMaxInstancesPerVM(1);
            // allow or disallow instances from SAME partition to deploy on same Container
            config.setMaxInstancesPerMachine(this.getMaxProcessingUnitInstancesFromSamePartitionPerMachine());
        }
        
        config.setClusterSchema("partitioned-sync2backup");
        config.setNumberOfInstances(numberOfInstances);
        config.setNumberOfBackups(getNumberOfBackupInstancesPerPartition());
        
        return config;
    }
    
    protected int calcNumberOfPartitionsFromMemoryRequirements() {
        
        long maximumMemoryCapacityInMB = new GridServiceContainerConfig(super.getElasticProperties()).getMaximumMemoryCapacityInMB();
                
        if (maximumMemoryCapacityInMB <= 0) {
            throw new IllegalStateException("memoryCapacityPerContainer is undefined.");    
        }
                
        double totalNumberOfInstances = Math.ceil(((double)getMaxMemoryCapacityInMB())/maximumMemoryCapacityInMB);
        int numberOfPartitions = (int) Math.ceil(totalNumberOfInstances / (getNumberOfBackupInstancesPerPartition()+1));
                
        return Math.max(1, numberOfPartitions);
    }

    protected int calcNumberOfPartitionsFromCpuRequirements(Admin admin) {
        
        int maximumNumberOfPrimaryInstances = 1;
        
        if (getMaxNumberOfCpuCores() > 0) {
            
            if (getMinNumberOfCpuCoresPerMachine() <= 0) {
                setMinNumberOfCpuCoresPerMachine(DiscoveredMachineProvisioningConfig.detectMinimumNumberOfCpuCoresPerMachine(admin));
            }
            
            maximumNumberOfPrimaryInstances =(int) Math.ceil(this.getMaxNumberOfCpuCores() / getMinNumberOfCpuCoresPerMachine());
        }
        return maximumNumberOfPrimaryInstances; 
    }

    public long getMaxMemoryCapacityInMB() {
        return maxMemoryCapacityInMB;
    }

    public void setMaxMemoryCapacityInMB(long maxMemoryCapacityInMB) {
        this.maxMemoryCapacityInMB = maxMemoryCapacityInMB;
    }

    public int getNumberOfBackupInstancesPerPartition() {
        return numberOfBackupInstancesPerPartition;
    }

    public void setNumberOfBackupInstancesPerPartition(int numberOfBackupInstancesPerPartition) {
        this.numberOfBackupInstancesPerPartition = numberOfBackupInstancesPerPartition;
    }

    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    public void setNumberOfPartitions(int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
    }

    public int getMaxProcessingUnitInstancesFromSamePartitionPerMachine() {
        return maxProcessingUnitInstancesFromSamePartitionPerMachine;
    }

    public void setMaxProcessingUnitInstancesFromSamePartitionPerMachine(
            int maxProcessingUnitInstancesFromSamePartitionPerMachine) {
        this.maxProcessingUnitInstancesFromSamePartitionPerMachine = maxProcessingUnitInstancesFromSamePartitionPerMachine;
    }

    public double getMaxNumberOfCpuCores() {
        return maxNumberOfCpuCores;
    }

    public void setMaxNumberOfCpuCores(double maxNumberOfCpuCores) {
        this.maxNumberOfCpuCores = maxNumberOfCpuCores;
    }

    @Deprecated
    public double getMinNumberOfCpuCoresPerMachine() {
        return minNumberOfCpuCoresPerMachine;
    }

    @Deprecated
    public void setMinNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        this.minNumberOfCpuCoresPerMachine = minNumberOfCpuCoresPerMachine;
    }
}
