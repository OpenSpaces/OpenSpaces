package org.openspaces.admin.space;

/**
 * Specified the replication target type
 * @author eitany
 * @since 8.0.3
 */
public enum ReplicationTargetType {
    /**
     * The target is a space instance
     */
    SPACE_INSTANCE,
    /**
     * The target is a mirror service
     */
    MIRROR_SERVICE,
    /**
     * The target is a gateway
     */
    GATEWAY

}
