package org.openspaces.admin.gateway;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.Space;

/**
 * A gateway is a special type of {@link ProcessingUnit} which is in charge of 
 * replication between different {@link Space}s.
 * 
 * @author eitany
 * @since 8.0.3
 */
public interface Gateway extends GridComponent{
    
    /**
     * Returns the name which is used by the other gateways to locate this gateway.
     */
    String getLookupName();
    
    /**
     * Returns the name of the processing unit hosting this gateway.
     */
    String getProcessingUnitName();
    
    /**
     * Returns the sink of this gateway or <code>null</code> if no sink exists in this gateway. 
     */
    Sink getSink();
}
