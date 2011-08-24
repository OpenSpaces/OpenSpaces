package org.openspaces.admin.gateway;

/**
 * Represents a single delegation target
 * @author eitany
 * @since 8.0.4
 */
public interface GatewayDelegatorTarget {
    
    /**
     * Returns the delegator this delegation target is part of. 
     */
    GatewayDelegator getDelegator();
    
    /**
     * Returns the delegated target gateway name.
     */
    String getTargetGatewayName();
    
    /**
     * Returns <code>true</code> if the delegation is done via another delegator or <code>false</code> if this delegator is connected
     * directly to the target gateway. 
     */
    boolean isDelegateThroughOtherGateway();
    
    /**
     * Returns the name of the gateway delegator this delegator is connected to which routes communication to the target gateway. If this
     * delegator is connected directly to the target gateway it will return <code>null</code>.
     * @see #isDelegateThroughOtherGateway()
     */
    String getDelegateThroughGatewayName();

}
