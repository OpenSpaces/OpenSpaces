package org.openspaces.admin.esm.deployment;

import java.io.Serializable;
import java.util.Properties;

import org.openspaces.grid.esm.ElasticScaleConfig;


/**
 * A deployment descriptor for a plain Data Grid.
 *
 * <blockquote>
 * <pre>
 * <b>Disclaimer:</b> This interface and the elastic data grid functionality is provided as a technology preview in XAP 7.1. 
 * As such, it is subject to API and behavior changes in the next XAP releases without going the usual deprecation process 
 * of the XAP API.
 * </pre>
 * </blockquote>
 */
public class ElasticDataGridDeployment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Properties contextProperties = new Properties();
    private final DeploymentContext context = new DeploymentContext();
    private ElasticScaleConfig config;
    private final String dataGridName;
    
    
    public ElasticDataGridDeployment(String dataGridName) {
        this.dataGridName = dataGridName;
    }
    
    /**
     * Set the isolation level of this data grid.
     * @param isolationLevel the isolation level requirement; Default {@link IsolationLevel#PUBLIC}.
     */
    public ElasticDataGridDeployment isolationLevel(IsolationLevel isolationLevel) {
        this.context.setIsolationLevel(isolationLevel);
        return this;
    }

    /**
     * Set the memory growth of this data grid. The parameter specifies size in bytes (b), kilobytes (k), megabytes (m), gigabytes (g);
     * 
     * @param minMemory
     *            minimum memory initially held for this data grid; Default "1g".
     * @param maxMemory
     *            maximum memory to allocate for this data grid; Default "10g".
     */
    public ElasticDataGridDeployment elasticity(String minMemory, String maxMemory) {
        this.context.setMinMemory(minMemory);
        this.context.setMaxMemory(maxMemory);
        return this;
    }

    /**
     * Set the availability of the data grid. A highly available data grid is one that has at least
     * one backup copy, for failing over to.
     * 
     * @param enabled
     *            <code>true</code> if data grid is highly available; false if the data is
     *            recoverable from some storage. Default is <code>true</code>.
     */
    public ElasticDataGridDeployment highlyAvailable(boolean enabled) {
        this.context.setHighlyAvailable(enabled);
        return this;
    }

    /**
     * Sets the initial Java heap size (the JVM -Xms argument) of a container hosting a processing unit.
     * For performance optimization you should have the initial heap size the same as the maximum size.
     * @param size The heap size in kilobytes (k), megabytes (m), gigabytes (g); Default "512m".
     */
    public ElasticDataGridDeployment initialJavaHeapSize(String size) {
        this.context.setInitialJavaHeapSize(size);
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
        this.context.setMaximumJavaHeapSize(size);
        return this;
    }
    
    /**
     * Adds a JVM level argument to the container hosting a processing unit. Note that the {@link #initialJavaHeapSize(String)} and
     * {@link #maximumJavaHeapSize(String)} already define the -Xms and -Xmx vm arguments.
     */
    public ElasticDataGridDeployment vmInputArgument(String vmInputArgument) {
        this.context.addVmArgument(vmInputArgument);
        return this;
    }
    
    /**
     * @return The data grid name this deployment was constructed with.
     */
    public String getDataGridName() {
        return dataGridName;
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
     * @return The deployment context
     */
    public DeploymentContext getContext() {
        return context;
    }
    
    /**
     * @return The context deploy time properties.
     */
    public Properties getContextProperties() {
        return contextProperties;
    }

    public ElasticDataGridDeployment addSla(SLA sla) {
        String descriptor = "";
        if (sla instanceof MemorySla) {
            descriptor = "sla="+MemorySla.class.getSimpleName() + ",threshold="+((MemorySla)sla).getThreshold()+"%/";
        }
        this.context.addSla(descriptor);
        return this;
    }
    
    public ElasticDataGridDeployment elasticScaleConfig(ElasticScaleConfig config) {
        this.config = config;
        return this;
    }
    
    public ElasticScaleConfig getElasticScaleConfig() {
        return config;
    }
}
