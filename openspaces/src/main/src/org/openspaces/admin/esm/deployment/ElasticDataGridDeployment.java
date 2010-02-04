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
     * Set the memory growth of this data grid. The parameter specifies size in bytes.
     * Abbreviations are 1KB (=1024 bytes), 1MB (=1024 KB), 1GB (=1024 MB), 1TG (=1024 GB). 
     *  
     * 
     * @param minMemory minimum memory initially held for this data grid; Default 1GB;
     * @param maxMemory maximum memory to allocate for this data grid; Default 10GB
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
     * @param enabled <code>true</code> if data grid is highly available; Default is <code>false</code>.
     */
    public ElasticDataGridDeployment highlyAvailable(boolean enabled) {
        this.context.setHighlyAvailable(enabled);
        return this;
    }
    
    /**
     * Sets the JVM size of a container hosting a processing unit.
     * 
     * @param jvmSize The JVM size; Default 512MB.
     */
    public ElasticDataGridDeployment jvmSize(String jvmSize) {
        this.context.setJvmSize(jvmSize);
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
