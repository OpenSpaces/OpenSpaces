package org.openspaces.admin.gateway;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * A Gateway Processing unit is the building block of a {@link Gateway}. 
 * It has one to one relationship with deployed {@link ProcessingUnit} which hosts this gateway component.
 * @author eitany
 * @since 8.0.4
 */
public interface GatewayProcessingUnit extends GridComponent{
    
    /**
     * Returns the {@link Gateway} this gateway processing unit belongs to.
     */
    Gateway getGateway();
    
    /**
     * Returns the hosting {@link ProcessingUnit}.
     */
    ProcessingUnit getProcessingUnit();

    /**
     * Returns the sink of this gateway or <code>null</code> if no sink exists in this gateway. 
     */
    GatewaySink getSink();
    
    /**
     * Returns the delegator of this gateway or <code>null</code> if no delegator exists in this gateway. 
     */
    GatewayDelegator getDelegator();
}
