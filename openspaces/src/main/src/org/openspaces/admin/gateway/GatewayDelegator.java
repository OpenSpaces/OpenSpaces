package org.openspaces.admin.gateway;

/**
 * A gateway delegator is used to delegate outgoing replication packets to other gateways. Acts as a communication connector such that a local space 
 * and relevant components need only to find the delegator locally in order to communicate with a remote gateway.
 * Delegators can be chained together to create a multi-hop delegation between gateways. 
 * @author eitany
 * @since 8.0.4
 * @see Gateway
 */
public interface GatewayDelegator {
    
    /**
     * Returns the gateway this sink is part of.
     */
    GatewayProcessingUnit getGatewayProcessingUnit();
    
    /**
     * Returns all the delegation targets of this delegator. 
     */
    GatewayDelegatorTarget[] getDelegationTargets(); 

}
