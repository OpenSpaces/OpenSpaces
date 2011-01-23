package org.openspaces.admin.pu.elastic;

import java.io.File;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.pu.elastic.AbstractElasticProcessingUnitDeployment;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.topology.AdvancedStatefulDeploymentTopology;
import org.openspaces.admin.pu.elastic.topology.ElasticStatefulDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

import com.gigaspaces.security.directory.UserDetails;

/**
 * Defines an elastic deployment of a processing unit that with an embedded space..
 * 
 * The advantage of this topology is that the code can access the data without
 * the network/serialization overhead and that space events can be used as code triggers.
 * The disadvantage compared to a stateless processing unit is that the ratio between 
 * the minimum and maximum number of containers is limited.
 * 
 * @see ElasticStatefulProcessingUnitDeployment
 * 
 * @author itaif
 */
public class ElasticStatefulProcessingUnitDeployment extends AbstractElasticProcessingUnitDeployment 
    implements ElasticStatefulDeploymentTopology , AdvancedStatefulDeploymentTopology {

    public static final String MAX_MEMORY_CAPACITY_MEGABYTES_DYNAMIC_PROPERTY = "max-memory-capacity-megabytes";
    public static final String MIN_MEMORY_CAPACITY_MEGABYTES_DYNAMIC_PROPERTY = "min-memory-capacity-megabytes";
    
    Map<String,String> scaleStrategy;
    private long maxMemoryCapacityInMB;
    private int numberOfBackupInstancesPerPartition = 1;
    private int numberOfPartitions;
    private int maxProcessingUnitInstancesFromSamePartitionPerMachine = 1;
    private double maxNumberOfCpuCores;
    private double minNumberOfCpuCoresPerMachine;
    
    /**
     * Constructs a stateful processing unit deployment based on the specified processing unit name (should
     * exists under the <code>[GS ROOT]/deploy</code> directory.
     */
    public ElasticStatefulProcessingUnitDeployment(String processingUnit) {
        super(processingUnit);
        
        // add an elastic property indicating the cluster schema partitioned-sync2backup
        new ProcessingUnitSchemaConfig(super.getElasticProperties()).setPartitionedSync2BackupSchema();        

        // this default context property ensures that during active-election (primary relocations) 
        // the proxy keeps retrying 20 times.
        // see also the wiki documentation on the active election property: cluster-config.groups.group.fail-over-policy.active-election.yield-time
        super.addContextPropertyDefault("space-config.proxy-settings.connection-retries","20");
    }
    
    /**
     * Constructs a stateful processing unit deployment based on the specified processing unit file path 
     * (points either to a processing unit jar/zip file or a directory).
     */
    public ElasticStatefulProcessingUnitDeployment(File processingUnit) {
        this(processingUnit.getAbsolutePath());
    }

    public ElasticStatefulProcessingUnitDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        this.maxMemoryCapacityInMB = unit.toMegaBytes(maxMemoryCapacity);
        return this;
    }

    public ElasticStatefulProcessingUnitDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        this.maxMemoryCapacityInMB = MemoryUnit.toMegaBytes(maxMemoryCapacity);
        return this;
    }

    public ElasticStatefulProcessingUnitDeployment highlyAvailable(boolean highlyAvailable) {
        numberOfBackupsPerPartition(highlyAvailable ? 1 : 0);
        return this;
    }
    
    public ElasticStatefulProcessingUnitDeployment numberOfBackupsPerPartition(int numberOfBackupsPerPartition) {
        this.numberOfBackupInstancesPerPartition = numberOfBackupsPerPartition;
        return this;
    }
    
    public ElasticStatefulProcessingUnitDeployment numberOfPartitions(int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
        return this;
    }

    protected ElasticStatefulProcessingUnitDeployment maxProcessingUnitInstancesFromSamePartitionPerMachine(int maxProcessingUnitInstancesFromSamePartitionPerMachine) {
        this.maxProcessingUnitInstancesFromSamePartitionPerMachine  = maxProcessingUnitInstancesFromSamePartitionPerMachine;
        return this;
    }
    
    public ElasticStatefulProcessingUnitDeployment maxNumberOfCpuCores(int maxNumberOfCpuCores) {
        this.maxNumberOfCpuCores = maxNumberOfCpuCores;
        return this;
    }

    public ElasticStatefulProcessingUnitDeployment minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        this.minNumberOfCpuCoresPerMachine = minNumberOfCpuCoresPerMachine;
        return this;
    }
    
    public ElasticStatefulProcessingUnitDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    public ElasticStatefulProcessingUnitDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }


    public ElasticStatefulProcessingUnitDeployment singleMachineDeployment() {
        this.maxProcessingUnitInstancesFromSamePartitionPerMachine(0);
        return this;
    }
    
    public ElasticStatefulProcessingUnitDeployment scale(EagerScaleConfig strategy) {
        return (ElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }

    public ElasticStatefulProcessingUnitDeployment scale(ManualCapacityScaleConfig strategy) {
        return (ElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }
    
    public ElasticStatefulProcessingUnitDeployment name(String name) {
        return (ElasticStatefulProcessingUnitDeployment) super.name(name);
    }
   
    public ElasticStatefulProcessingUnitDeployment addContextProperty(String key, String value) {
        return (ElasticStatefulProcessingUnitDeployment) super.addContextProperty(key, value);
    }

    public ElasticStatefulProcessingUnitDeployment secured(boolean secured) {
        return (ElasticStatefulProcessingUnitDeployment) super.secured(secured);
    }

    public ElasticStatefulProcessingUnitDeployment userDetails(UserDetails userDetails) {
        return (ElasticStatefulProcessingUnitDeployment) super.userDetails(userDetails);
    }

    public ElasticStatefulProcessingUnitDeployment userDetails(String userName, String password) {
        return (ElasticStatefulProcessingUnitDeployment) super.userDetails(userName, password);
    }
    
    public ElasticStatefulProcessingUnitDeployment useScriptToStartContainer() {
        return (ElasticStatefulProcessingUnitDeployment) super.useScriptToStartContainer();
    }

    public ElasticStatefulProcessingUnitDeployment overrideCommandLineArguments() {
        return (ElasticStatefulProcessingUnitDeployment) super.overrideCommandLineArguments();
    }

    public ElasticStatefulProcessingUnitDeployment commandLineArgument(String vmInputArgument) {
        return (ElasticStatefulProcessingUnitDeployment) super.commandLineArgument(vmInputArgument);
    }

    public ElasticStatefulProcessingUnitDeployment environmentVariable(String name, String value) {
        return (ElasticStatefulProcessingUnitDeployment) super.environmentVariable(name, value);
    }
    
    public ElasticStatefulProcessingUnitDeployment machineProvisioning(ElasticMachineProvisioningConfig config) {
        if (minNumberOfCpuCoresPerMachine <= 0) {
            // if not overrided, try to figure out from machine provisioning
            minNumberOfCpuCoresPerMachine = config.getMinimumNumberOfCpuCoresPerMachine();
            if (minNumberOfCpuCoresPerMachine <= 0) {
                throw new AdminException("Elastic Machine Provisioning configuration must supply the expected minimum number of CPU cores per machine.");
            }
        }
        return (ElasticStatefulProcessingUnitDeployment) super.machineProvisioning(config);
    }
    
    public ProcessingUnitDeployment toProcessingUnitDeployment(Admin admin) {
      
        ProcessingUnitDeployment deployment = super.toProcessingUnitDeployment();
        
        if (this.maxMemoryCapacityInMB == 0 && this.numberOfPartitions == 0) {
            throw new IllegalStateException("maxMemoryCapacity must be defined.");
        }
        
        if (this.maxMemoryCapacityInMB != 0 && this.numberOfPartitions != 0) {
            throw new IllegalStateException("numberOfPartitions conflicts with maxMemoryCapacity. Please specify only one of these properties.");
        }
        
        if (this.maxNumberOfCpuCores != 0 && this.numberOfPartitions != 0) {
            throw new IllegalStateException("numberOfPartitions conflicts with maxNumberOfCpuCores. Please specify only one of these properties.");
        }

        int numberOfInstances = this.numberOfPartitions;
        if (numberOfInstances == 0) {
            numberOfInstances = Math.max(calcNumberOfPartitionsFromMemoryRequirements(),calcNumberOfPartitionsFromCpuRequirements(admin));
        }
        
        if (numberOfBackupInstancesPerPartition == 0) {
            // allow instances from DIFFERENT partitions to deploy on same Container
            deployment.maxInstancesPerMachine(0);   
            deployment.maxInstancesPerVM(0);
        }
        else {
            // disallow instances from SAME partition to deploy on same Container
            deployment.maxInstancesPerVM(1);
            // allow or disallow instances from SAME partition to deploy on same Container
            deployment.maxInstancesPerMachine(this.maxProcessingUnitInstancesFromSamePartitionPerMachine);
        }
        
        deployment
        .partitioned(numberOfInstances, this.numberOfBackupInstancesPerPartition);
      
        return deployment;
    }
    
    protected int calcNumberOfPartitionsFromMemoryRequirements() {
        
        long maximumJavaHeapSizeMegabytes = new GridServiceContainerConfig(super.getElasticProperties()).getMaximumJavaHeapSizeInMB();
                
        if (maximumJavaHeapSizeMegabytes == 0) {
            throw new IllegalStateException("-Xmx CommandLineArgument is undefined.");    
        }
                
        double totalNumberOfInstances = Math.ceil(((double)maxMemoryCapacityInMB)/maximumJavaHeapSizeMegabytes);
        int numberOfPartitions = (int) Math.ceil(totalNumberOfInstances / (numberOfBackupInstancesPerPartition+1));
                
        return Math.max(1, numberOfPartitions);
    }

    protected int calcNumberOfPartitionsFromCpuRequirements(Admin admin) {
        
        int maximumNumberOfPrimaryInstances = 1;
        
        if (maxNumberOfCpuCores > 0) {
            
            if (minNumberOfCpuCoresPerMachine <= 0) {
                minNumberOfCpuCoresPerMachine = findMinimumNumberOfCpuCoresPerMachine(admin);
            }

            maximumNumberOfPrimaryInstances =(int) Math.ceil(this.maxNumberOfCpuCores / minNumberOfCpuCoresPerMachine);
        }
        return maximumNumberOfPrimaryInstances; 
    }

    private double findMinimumNumberOfCpuCoresPerMachine(Admin admin) {
        // No machineProvisioning is defined means that the server will use whatever machine it could find.
        // so we just go over all machines and calculate the minimum number of cpu cores per machine.
        final GridServiceAgent[] agents = admin.getGridServiceAgents().getAgents();
        if (agents.length == 0) {
            throw new AdminException("Cannot determine minimum number of cpu cores per machine. Please use new AdvancedElasticStatefulProcessingUnit().minNumberOfCpuCoresPerMachine() to specify this figure.");
        }
        double minCoresPerMachine = getNumberOfCpuCores(agents[0].getMachine());
        for (final GridServiceAgent agent : agents) {
            final double cores = getNumberOfCpuCores(agent.getMachine());
            if (cores <= 0) {
                throw new AdminException("Cannot determine number of cpu cores on machine " + agent.getMachine().getHostAddress());
            }
            if (minCoresPerMachine < cores) {
                minCoresPerMachine = cores; 
            }
        }
        return minCoresPerMachine;
    }

    private double getNumberOfCpuCores(Machine machine) {
        return machine.getOperatingSystem().getDetails().getAvailableProcessors();
    }    
}