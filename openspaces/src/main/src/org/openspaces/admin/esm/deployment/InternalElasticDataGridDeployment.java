package org.openspaces.admin.esm.deployment;

import java.io.Serializable;

/**
 * An internal extension to the {@link ElasticDataGridDeployment} API.
 */
public class InternalElasticDataGridDeployment implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String dataGridName;
    private final DeploymentContext deploymentContext = new DeploymentContext();
    
    public InternalElasticDataGridDeployment(String dataGridName) {
        this.dataGridName = dataGridName;
    }
    
    public DeploymentContext getDeploymentContext() {
        return deploymentContext;
    }
    
    /**
     * @return The data grid name this deployment was constructed with.
     */
    public String getDataGridName() {
        return dataGridName;
    }
}
