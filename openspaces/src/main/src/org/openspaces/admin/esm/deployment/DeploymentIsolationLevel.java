package org.openspaces.admin.esm.deployment;

/**
 * Specifies the deployment isolation level (per processing unit).
 * <p>
 * {@link #PUBLIC} - A machine is shared by multiple tenants. Deployment of one tenant can co-exist
 * with other deployments.
 * <p>
 * {@link #SHARED} - A machine is shared by a specific tenant. No other tenant can use this machine.
 * Deployments of one tenant can co-exist with other deployments of the same tenant.
 * <p>
 * {@link #DEDICATED} - A machine is dedicated to a single tenant. No other tenant can use this
 * machine. Only a single deployment will be exist on this machine.
 * 
 * <blockquote>
 * <pre>
 * <b>Disclaimer:</b> This interface and the elastic data grid functionality is provided as a technology preview in XAP 7.1. 
 * As such, it is subject to API and behavior changes in the next XAP releases without going through the usual deprecation process 
 * of the XAP API.
 * </pre>
 * </blockquote>
 */
public enum DeploymentIsolationLevel {
    /** public to all tenants */
//    PUBLIC,
    /** shared by a specific tenant */
//    SHARED,
    /** dedicated to a single tenant */
    DEDICATED;
}
