package org.openspaces.admin.esm.deployment;

import java.io.Serializable;


/**
 * A deployment descriptor for a plain Data Grid.
 *  
 * @author Moran Avigdor
 */
public class ElasticDataGridDeployment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final DeploymentContext context = new DeploymentContext();
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
     * @return The data grid name this deployment was constructed with.
     */
    public String getDataGridName() {
        return dataGridName;
    }
    
    /**
     * @return The deployment context
     */
    public DeploymentContext getContext() {
        return context;
    }

    public ElasticDataGridDeployment addSla(SLA sla) {
        String descriptor = "";
        if (sla instanceof MemorySla) {
            descriptor = "sla="+MemorySla.class.getSimpleName() + ",threshold="+((MemorySla)sla).getThreshold()+"/";
        }
        this.context.addSla(descriptor);
        return this;
    }
}
