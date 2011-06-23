package org.openspaces.admin.pu;

/**
 * An enumeration which specifies the type of the processing unit.
 * 
 * @since 8.0.3
 * @author Moran Avigdor
 */
public enum ProcessingUnitType {
    UNKNOWN,
    STATELESS,
    STATEFUL,
    MIRROR,
    WEB,
    UNIVERSAL,
    GATEWAY
}
