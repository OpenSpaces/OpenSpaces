package org.openspaces.admin.esm.deployment;

import java.util.Properties;

import org.openspaces.grid.esm.ElasticScaleHandler;
import org.openspaces.grid.esm.ElasticScaleHandlerConfig;

/**
 * An elastic data-grid deployment descriptor for deploying a plain data-grid.
 * <p>
 * The data-grid context properties can be modified by {@link #addContextProperty(String, String)}.
 * The default is a Highly-available data-grid, consisting of 1-10 gigabytes of memory spanned
 * across a partitioned cluster of 10,1. Each Grid Service Container JVM is set to -Xmn512m and
 * -Xmx512m.
 * 
 * <blockquote>
 * <pre>
 * <b>Disclaimer:</b> This interface and the elastic data grid functionality is provided as a technology preview in XAP 7.1. 
 * As such, it is subject to API and behavior changes in the next XAP releases without going through the usual deprecation process 
 * of the XAP API.
 * </pre>
 * </blockquote>
 */
public class ElasticDataGridDeployment extends InternalElasticDataGridDeployment {
    private static final long serialVersionUID = 1L;
    private final Properties contextProperties = new Properties();
    private ElasticScaleHandlerConfig config;
    
    /**
     * Constructs an Elastic data-grid with the specified name.
     * @param dataGridName The data-grid name.
     */
    public ElasticDataGridDeployment(String dataGridName) {
        super(dataGridName);
    }
    
    public ElasticDataGridDeployment dedicatedDeploymentIsolation() {
        getDeploymentContext().setDeploymentIsolationLevel(DeploymentIsolationLevel.DEDICATED);
        return this;
    }
    
    public ElasticDataGridDeployment publicDeploymentIsolation() {
        getDeploymentContext().setDeploymentIsolationLevel(DeploymentIsolationLevel.PUBLIC);
        return this;
    }
    
    public ElasticDataGridDeployment sharedDeploymentIsolation(String tenant) {
        getDeploymentContext().setDeploymentIsolationLevel(DeploymentIsolationLevel.SHARED);
        getDeploymentContext().setTenant(tenant);
        return this;
    }
    
    /**
     * Set the memory capacity growth of this data grid. The parameter specifies size in bytes (b),
     * kilobytes (k), megabytes (m), gigabytes (g). These two control respectively -Xmn and -Xmx
     * of the Grid Service Container JVM. Default it "1g"-"10g".
     * 
     * @param minMemory
     *            minimum memory initially held for this data grid; Default "1g".
     * @param maxMemory
     *            maximum memory to allocate for this data grid; Default "10g".
     */
    public ElasticDataGridDeployment capacity(String minMemory, String maxMemory) {
        getDeploymentContext().setMinMemory(minMemory);
        getDeploymentContext().setMaxMemory(maxMemory);
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
    public ElasticDataGridDeployment highlyAvailable(boolean enabled) {
        getDeploymentContext().setHighlyAvailable(enabled);
        return this;
    }

    /**
     * Sets the initial Java heap size (the JVM -Xms argument) of a container hosting a processing unit.
     * For performance optimization you should have the initial heap size the same as the maximum size.
     * @param size The heap size in kilobytes (k), megabytes (m), gigabytes (g); Default "512m".
     */
    public ElasticDataGridDeployment initialJavaHeapSize(String size) {
        getDeploymentContext().setInitialJavaHeapSize(size);
        return this;
    }

    /**
     * Sets the maximum Java heap size (the JVM -Xmx argument) of a container hosting a processing
     * unit. In many cases the heap size is determined based on the operating system: for a 32-bit
     * OS, a 2 GB maximum heap size is recommended, and for a 64-bit OS, a 6-10 MB maximum heap size
     * is recommended.
     * 
     * @param size The heap size in kilobytes (k), megabytes (m), gigabytes (g); Default "512m".
     */
    public ElasticDataGridDeployment maximumJavaHeapSize(String size) {
        getDeploymentContext().setMaximumJavaHeapSize(size);
        return this;
    }

    /**
     * Adds a single JVM level argument to the container hosting a processing unit. Note that the
     * {@link #initialJavaHeapSize(String)} and {@link #maximumJavaHeapSize(String)} already define
     * the -Xms and -Xmx vm arguments.
     */
    public ElasticDataGridDeployment vmInputArgument(String vmInputArgument) {
        if (vmInputArgument.length() == 0) {
            throw new IllegalArgumentException("VM input argument should not be empty");
        }
        getDeploymentContext().addVmArgument(vmInputArgument);
        return this;
    }
    
    /**
     * Adds a context deploy time property overriding any <code>${...}</code> defined within a processing
     * unit configuration.
     */
    public ElasticDataGridDeployment addContextProperty(String key, String value) {
        if (key.contains(";") || value.contains(";"))
            throw new IllegalArgumentException("properties should not contain the ';' delimeter");
        
        contextProperties.put(key, value);
        return this;
    }
    
    /**
     * Sets the number of partitions to use (number of primaries if highly available). This setting will override the default
     * calculated value for the number of partitions derived from the {@link #capacity(String, String)} parameters.
     * 
     * @param numberOfPartitions The number of partitions required.
     */
    public ElasticDataGridDeployment setPartitions(int numberOfPartitions) {
        if (numberOfPartitions < 1)
            throw new IllegalArgumentException("invalid partition size [" + numberOfPartitions+"] - must be at least 1");
        
        getDeploymentContext().setPartitions(numberOfPartitions);
        return this;
    }
       
    /**
     * @return The context deploy time properties.
     */
    public Properties getContextProperties() {
        return contextProperties;
    }

    /**
     * Adds an SLA to monitor and auto-scale if necessary.
     * @see MemorySla
     * 
     * @param sla an SLA descriptor
     */
    public ElasticDataGridDeployment addSla(SLA sla) {
        String descriptor = "";
        if (sla instanceof MemorySla) {
            descriptor = MemorySlaSerializer.toString(((MemorySla)sla));
        }
        getDeploymentContext().addSla(descriptor+"/");
        return this;
    }

    /**
     * Set an {@link ElasticScaleHandler} implementation configuration. The scale-handler (and its
     * state) is per-deployment. The scale-handler API will be called upon when need of a GSC (either
     * on an available machine, or a new machine)
     * 
     * @param config The elastic scale handler configuration
     */
    public ElasticDataGridDeployment elasticScaleHandler(ElasticScaleHandlerConfig config) {
        this.config = config;
        return this;
    }
    
    /**
     * @return The elastic scale configuration
     */
    public ElasticScaleHandlerConfig getElasticScaleConfig() {
        return config;
    }
}
