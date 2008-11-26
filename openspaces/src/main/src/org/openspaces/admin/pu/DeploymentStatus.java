package org.openspaces.admin.pu;

/**
 * @author kimchy
 */
public enum DeploymentStatus {
    /**
     * Indicates the Processing Unit is not deployed
     */
    UNDEPLOYED,
    /**
     * Indicates the Processing Unit is scheduled for deployment
     */
    SCHEDULED,
    /**
     * Indicates the Processing Unit is deployed
     */
    DEPLOYED,
    /**
     * Indicates the Processing Unit is deployed and is broken, where all
     * required services are not available
     */
    BROKEN,
    /**
     * Indicates the Processing Unit is deployed and is compromised, where
     * some specified services are not available
     */
    COMPROMISED,
    /**
     * Indicates the Processing Unit is deployed and is intact, where all
     * specified services are available
     */
    INTACT
}
