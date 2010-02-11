package org.openspaces.admin.esm.deployment;

/**
 * Specifies the isolation level of a deployment (per processing unit).
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
 * @author Moran Avigdor
 */
public enum IsolationLevel {
    /** public to all tenants */
//    PUBLIC,
    /** shared by a specific tenant */
//    SHARED,
    /** dedicated to a single tenant */
    DEDICATED;
}
