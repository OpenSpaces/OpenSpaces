package org.openspaces.admin.pu.elastic;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.openspaces.admin.internal.pu.elastic.ElasticScaleHandlerConfig;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.grid.esm.ElasticScaleHandler;
import org.openspaces.grid.esm.MemorySettings;

public class ElasticProcessingUnitDeployment {

    private final String processingUnit;
    
    private String name;
    
    private int numberOfInstances = 0;
    
    private int numberOfBackups = 1; //highly available by default

    private final Properties contextProperties = new Properties();
    
    private String initialJavaHeapSize;
    private String maximumJavaHeapSize;
    
    private DeploymentIsolationLevel isolation = DeploymentIsolationLevel.DEDICATED;

    private ElasticScaleHandlerConfig elasticScaleHandlerConfig;

    private String vmInputArguments;
    
    private String slaDescriptors;

    private String tenant;

    private String minMemory;

    private String maxMemory;
    
    /**
     * Constructs a processing unit deployment based on the specified processing unit name (should
     * exists under the <code>[GS ROOT]/deploy</code> directory.
     */
    public ElasticProcessingUnitDeployment(String processingUnit) {
        this.processingUnit = processingUnit;
    }

    /**
     * Constructs a processing unit deployment based on the specified processing unit file path (points either
     * to a processing unit jar/zip file or a directory).
     */
    public ElasticProcessingUnitDeployment(File processingUnit) {
        this.processingUnit = processingUnit.getAbsolutePath();
    }

    /**
     * Returns the processing unit that will be deployed.
     */
    public String getProcessingUnit() {
        return processingUnit;
    }
    
    /**
     * Sets the processing unit name that will be deployed. By default it will be based on the
     * parameter passed in the constructor.
     */
    public ElasticProcessingUnitDeployment name(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * Sets the number of instances that will be deployed as part of this processing unit instance. This setting will override the default
     * calculated value for the number of partitions derived from the {@link #capacity(String, String)} parameters.
     */
    public ElasticProcessingUnitDeployment numberOfInstances(int numberOfInstances) {
        if (numberOfInstances < 1) {
            throw new IllegalArgumentException("invalid number of instances [" + numberOfInstances +"] - must be at least 1");
        }
        this.numberOfInstances = numberOfInstances;
        return this;
    }

    /**
     * Sets the number of backups per instance of the space deployment.
     * Setting 0 backup is same as calling highlyAvailable(false)
     * Setting 1 backup is the default and is the same as calling highlyAvailable(true)
     */
    public ElasticProcessingUnitDeployment numberOfBackups(int numberOfBackups) {
        if (numberOfBackups != 0 && numberOfBackups != 1) {
            throw new IllegalArgumentException("number of backups could be 0 or 1");
        }
        this.numberOfInstances = numberOfBackups;
        return this;
    }
    
    /**
     * Allow the processing unit to share the same machine with other deployments.
     */
    public ElasticProcessingUnitDeployment publicDeploymentIsolation() {
        setDeploymentIsolationLevel(DeploymentIsolationLevel.PUBLIC);
        return this;
    }

    public void sla(SLA sla) {
        String descriptor = "";
        if (sla instanceof MemorySla) {
            descriptor = MemorySlaSerializer.toString(((MemorySla)sla));
        }
        slaDescriptors += descriptor+"/";
    }
    
    /**
     * Set an {@link ElasticScaleHandler} implementation configuration. The scale-handler (and its
     * state) is per-deployment. The scale-handler API will be called upon when need of a GSC (either
     * on an available machine, or a new machine)
     * 
     * @param classname The elastic scale handler class name
     * @param properties The elastic scale handler properties.
     */
    public ElasticProcessingUnitDeployment elasticScaleHandler(String classname, Map<String,String> properties) {
        this.elasticScaleHandlerConfig = new ElasticScaleHandlerConfig(classname,properties);
        return this;
    }
    
    /**
     * Sets a context deploy time property overriding any <code>${...}</code> defined within a processing
     * unit configuration.
     */
    public ElasticProcessingUnitDeployment setContextProperty(String key, String value) {
        if (key.contains(";") || value.contains(";"))
            throw new IllegalArgumentException("properties should not contain the ';' delimeter");
        
        contextProperties.put(key, value);
        return this;
    }
    
    /**
     * Adds a single JVM level argument to the container hosting a processing unit. Note that the
     * {@link #initialJavaHeapSize(String)} and {@link #maximumJavaHeapSize(String)} already define
     * the -Xms and -Xmx vm arguments.
     */
    public ElasticProcessingUnitDeployment vmInputArgument(String arg) {
        if (arg.length() == 0) {
            throw new IllegalArgumentException("VM input argument should not be empty");
        }
        if (arg.contains("-Xms") || arg.contains("-Xmx")) {
            throw new IllegalArgumentException("Java Heap size should be set using the 'initialJavaHeapSize' and 'maximumJavaHeapSize' methods");
        }
        if (vmInputArguments  == null || vmInputArguments.length() == 0) {
            vmInputArguments = arg;
        } else {
            vmInputArguments += "," + arg;
        }

        return this;
    }

    /**
     * @return The context deploy time properties.
     */
    public Properties getContextProperties() {
        return contextProperties;
    }

    /**
     * Allow this data-grid deployment to co-exist and share the same machine resources with other
     * deployments of this tenant - only.
     * 
     * @param tenant A name representing the tenant.
     */
    public ElasticProcessingUnitDeployment sharedDeploymentIsolation(String tenant) {
        this.isolation = DeploymentIsolationLevel.SHARED;
        this.tenant = tenant;
        return this;
    }
    
    private void setDeploymentIsolationLevel(DeploymentIsolationLevel isolation) {
        this.isolation = isolation;
    }

    /**
     * Set the memory capacity growth of this data grid. The parameter specifies size in bytes (b),
     * kilobytes (k), megabytes (m), gigabytes (g). These two control respectively -Xms and -Xmx
     * of the Grid Service Container JVM. Default it "1g"-"10g".
     * 
     * @param minMemory
     *            minimum memory initially held for this data grid; Default "1g".
     * @param maxMemory
     *            maximum memory to allocate for this data grid; Default "10g".
     */
    public ElasticProcessingUnitDeployment capacity(String minMemory, String maxMemory) {
        this.minMemory = minMemory;
        this.maxMemory = maxMemory;
        return this;
    }
    
    /**
     * Set the availability of the data grid. A highly available data grid is one that has at least
     * one backup copy per primary, for failing over to.
     * 
     * @param enabled
     *            <code>true</code> if data grid is highly available; false if the data is
     *            recoverable from some storage. Default is <code>true</code>.
     */
    public ElasticProcessingUnitDeployment highlyAvailable(boolean enabled) {
        this.numberOfBackups = enabled ? 1 : 0;
        return this;
    }

    /**
     * Sets the initial Java heap size (the JVM -Xms argument) of a container hosting a processing unit.
     * For performance optimization you should have the initial heap size the same as the maximum size.
     * @param size The heap size in kilobytes (k), megabytes (m), gigabytes (g); Default "512m".
     */
    public ElasticProcessingUnitDeployment initialJavaHeapSize(String size) {
        this.initialJavaHeapSize = size;
        return this;
    }

    /**
     * Sets the maximum Java heap size (the JVM -Xmx argument) of a container hosting a processing
     * unit. In many cases the heap size is determined based on the operating system: for a 32-bit
     * OS, a 2 GB maximum heap size is recommended, and for a 64-bit OS, a 6-10 MB maximum heap size
     * is recommended.
     * <p>
     * If set below the {@link #initialJavaHeapSize(String)} then the -Xms setting will correspond
     * to the -Xmx setting.
     * 
     * @param size
     *            The heap size in kilobytes (k), megabytes (m), gigabytes (g); Default "512m".
     */
    public ElasticProcessingUnitDeployment maximumJavaHeapSize(String size) {
        this.maximumJavaHeapSize = size;
        return this;
    }
    
    
    /**
     * Restrict this data-grid deployment to machines not shared by other deployments.
     */
    public ElasticProcessingUnitDeployment dedicatedDeploymentIsolation() {
        this.isolation = DeploymentIsolationLevel.DEDICATED;
        return this;
    }
   
    public ProcessingUnitDeployment toProcessingUnitDeployment() {

        if (maxMemory == null) {
            throw new IllegalStateException("ElasticProcessingUnitDeployment capacity is required and cannot be null.");
        }

        if (maximumJavaHeapSize == null) {
            throw new IllegalStateException("ElasticProcessingUnitDeployment maximum Java heap size is required and cannot be null.");    
        }

        if (maxMemory != null && minMemory == null) {
            minMemory = maxMemory;
        }

        if (maximumJavaHeapSize != null &&
                (initialJavaHeapSize == null ||
                 MemorySettings.valueOf(initialJavaHeapSize).isGreaterThan(MemorySettings.valueOf(maximumJavaHeapSize)))) {
            initialJavaHeapSize = maximumJavaHeapSize;
        }

        if (name == null) {
            setDefaultName();
        }

        String tenantPrefix = tenant == null || tenant.length()==0 ? "" : tenant + "-"; 
        final String zoneName = tenantPrefix+name;
        ProcessingUnitDeployment deployment = new ProcessingUnitDeployment(processingUnit);
        deployment.addZone(zoneName);

        int numberOfParitions = calculateNumberOfPartitions();
        if (numberOfInstances != 0) {
            if (numberOfInstances < numberOfParitions) {
                throw new IllegalArgumentException("Number of instances [" + numberOfInstances
                        + "] must be greater than [" + numberOfParitions
                        + "] to meet the minimum requested capacity of [" + maxMemory + "]");
            }
            numberOfParitions = numberOfInstances;
        }
        
        if (numberOfBackups == 1) {
            deployment.maxInstancesPerMachine(1);
            deployment.partitioned(numberOfParitions, 1);
        } else {
            deployment.partitioned(numberOfParitions, 0);
        }
        

        
        deployment.setContextProperty(ElasticDeploymentContextProperties.ELASTIC, "true");
        deployment.setContextProperty(ElasticDeploymentContextProperties.MIN_MEMORY, minMemory);
        deployment.setContextProperty(ElasticDeploymentContextProperties.MAX_MEMORY, maxMemory);
        deployment.setContextProperty(ElasticDeploymentContextProperties.INITIAL_JAVA_HEAP_SIZE, initialJavaHeapSize);
        deployment.setContextProperty(ElasticDeploymentContextProperties.MAXIMUM_JAVA_HEAP_SIZE, maximumJavaHeapSize);
        deployment.setContextProperty(ElasticDeploymentContextProperties.DEPLOYMENT_ISOLATION, isolation.name());
        deployment.setContextProperty(ElasticDeploymentContextProperties.ZONE, zoneName);
        if (slaDescriptors != null) {
            deployment.setContextProperty(ElasticDeploymentContextProperties.SLA, slaDescriptors);
        }
        if (tenant != null) {
            deployment.setContextProperty(ElasticDeploymentContextProperties.TENANT, tenant);
        }

        if (elasticScaleHandlerConfig != null) {
            deployment.setContextProperty(
                    ElasticDeploymentContextProperties.ELASTIC_SCALE_CONFIG,
                    elasticScaleHandlerConfig.toString());
        }
        
        if (!contextProperties.isEmpty()) {
            Set<Entry<Object,Object>> entrySet = contextProperties.entrySet();
            for (Entry<Object,Object> entry : entrySet) {
                deployment.setContextProperty((String)entry.getKey(), (String)entry.getValue());
            }
        }
        
        if (vmInputArguments != null) {
           deployment.setContextProperty(ElasticDeploymentContextProperties.VM_ARGUMENTS, 
                   vmInputArguments); 
        }

        return deployment;
    }

    private void setDefaultName() {
        //replace whitespaces
        name = processingUnit.replace(' ', '_');

        //trim closing slash
        if (name.endsWith("/") || name.endsWith("\\")) {
            name = name.substring(0,name.length()-1);
        }
        // pick directory/file name
        int seperatorIndex = Math.max(name.lastIndexOf("/"),name.lastIndexOf("\\"));
        if (seperatorIndex >= 0 && seperatorIndex < name.length()-1 ) {
            name = name.substring(seperatorIndex+1,name.length());
        }
        // remove file extension
        if (name.endsWith(".zip") ||
            name.endsWith(".jar") ||
            name.endsWith(".war")) {

            name = name.substring(0, name.length() - 4);
        }
    }

    private int calculateNumberOfPartitions() {
        
        int numberOfPartitions = MemorySettings.valueOf(maxMemory).floorDividedBy(maximumJavaHeapSize);
        if (numberOfBackups == 1) {
            numberOfPartitions /= 2;
        }
        
        return Math.max(1, numberOfPartitions);
    }

}
