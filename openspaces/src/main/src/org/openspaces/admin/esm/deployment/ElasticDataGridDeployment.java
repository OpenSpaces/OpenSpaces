package org.openspaces.admin.esm.deployment;

import java.io.Serializable;

/**
 * A deployment descriptor for a plain Data Grid.
 *  
 * @author Moran Avigdor
 */
public class ElasticDataGridDeployment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private IsolationLevel isolationLevel = IsolationLevel.SHARED_PUBLIC;
    private final String dataGridName;
    private String minMemory = "1GB";
    private String maxMemory = "10GB";
    private String jvmSize = "512MB";
    private boolean highlyAvailable;
    
    
    public ElasticDataGridDeployment(String dataGridName) {
        this.dataGridName = dataGridName;
    }
    
    /**
     * Set the isolation level of this data grid.
     * @param isolationLevel the isolation level requirement; Default {@link IsolationLevel#SHARED_PUBLIC}.
     */
    public ElasticDataGridDeployment isolationLevel(IsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
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
        this.minMemory = minMemory;
        this.maxMemory = maxMemory;
        return this;
    }

    /**
     * Set the availability of the data grid. A highly available data grid is one that has at least
     * one backup copy, for failing over to.
     * 
     * @param enabled <code>true</code> if data grid is highly available; Default is <code>false</code>.
     */
    public ElasticDataGridDeployment highlyAvailable(boolean enabled) {
        this.highlyAvailable = enabled;
        return this;
    }
    
    public ElasticDataGridDeployment jvmSize(String jvmSize) {
        this.jvmSize = jvmSize;
        return this;
    }
    
    /**
     * @return The data grid name this deployment was constructed with.
     */
    public String getDataGridName() {
        return dataGridName;
    }
    
    public String getMinMemory() {
        return minMemory;
    }
    
    public String getMaxMemory() {
        return maxMemory;
    }
    
    public IsolationLevel getIsolationLevel() {
        return isolationLevel;
    }
    
    public boolean isHighlyAvailable() {
        return highlyAvailable;
    }
    
    public String getJvmSize() {
        return jvmSize;
    }
}
