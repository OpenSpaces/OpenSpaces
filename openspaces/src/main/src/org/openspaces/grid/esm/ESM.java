package org.openspaces.grid.esm;


import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;


/**
 * The Elastic Service Manager (ESM) provides the support to deploy, manage and
 * monitor elastic services in the Grid/Cloud.
 */
public interface ESM extends com.gigaspaces.grid.esm.ESM {

    void deploy(ElasticDataGridDeployment deployment);
}
