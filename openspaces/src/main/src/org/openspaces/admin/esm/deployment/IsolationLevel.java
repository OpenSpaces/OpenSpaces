package org.openspaces.admin.esm.deployment;

/**
 * Specifies the isolation level of a deployment -
 * <p>
 * {@link #SHARED_PUBLIC} - Deployment can co-exist with other deployments on the same (Virtual) Machine, thus sharing compute
 * resources with other tenants;
 * <p>
 * {@link #SHARED_PRIVATE} - Deployment can only co-exist with other deployments of a specific
 * tenant, meaning no other tenant can use these resources;
 * <p>
 * {@link #DEDICATED_PRIVATE} - Deployment should not co-exist with any other deployments, thus
 * allocating a dedicated resource for this tenant.
 * 
 * @author Moran Avigdor
 */
public enum IsolationLevel {
    /** share resources with other tenants */
    SHARED_PUBLIC,
    /** share resources with other deployments of this tenant */
    SHARED_PRIVATE,
    /** dedicate resources only to this tenant */
    DEDICATED_PRIVATE
}
