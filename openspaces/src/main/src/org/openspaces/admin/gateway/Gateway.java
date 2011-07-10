package org.openspaces.admin.gateway;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;

/**
 * A gateway is a composition of one or more of {@link GatewayProcessingUnit}s, and it is in charge of 
 * replication between different {@link Space}s. e.g. Replication between two sites over WAN.
 * 
 * @author eitany
 * @since 8.0.4
 */
public interface Gateway extends Iterable<GatewayProcessingUnit>{
    
    /**
     * Returns all the currently deployed {@link GatewayProcessingUnit}s. 
     */
    GatewayProcessingUnit[] getGatewayProcessingUnits();
    
    /**
     * Returns the name which is used by the other gateways to locate this gateway.
     */
    String getName();
    
    /**
     * Waits for the default timeout specified by {@link Admin#setDefaultTimeout(long, TimeUnit)} till 
     * at least the provided number of Gateway Processing Unit Instances are up. Returns <code>true</code> if
     * the specified number of gateway processing units are deployed, <code> false</code> otherwise;
     */
    boolean waitFor(int numberOfGatewayProcessingUnits);
    
    boolean waitFor(int numberOfGatewayProcessingUnits, long timeout, TimeUnit timeUnit);
    
    GatewayProcessingUnit waitForGatewayProcessingUnit(String processingUnitName);
    
    GatewayProcessingUnit waitForGatewayProcessingUnit(String processingUnitName, long timeout, TimeUnit timeUnit);
    
    GatewayProcessingUnit getGatewayProcessingUnit(String processingUnitName);
    
    Map<String, GatewayProcessingUnit> getNames();
    
    GatewaySink getSink(String sourceGatewayName);
    
    GatewaySink waitForSink(String sourceGatewayName);
    
    GatewaySink waitForSink(String sourceGatewayName, long timeout, TimeUnit timeUnit);
    
    GatewaySinkSource getSinkSource(String sourceGatewayName);
    
    GatewaySinkSource waitForSinkSource(String sourceGatewayName);
    
    GatewaySinkSource waitForSinkSource(String sourceGatewayName, long timeout, TimeUnit timeUnit);
    
    GatewayDelegator getDelegator(String targetGatewayName);
    
    GatewayDelegator waitForDelegator(String targetGatewayName);
    
    GatewayDelegator waitForDelegator(String targetGatewayName, long timeout, TimeUnit timeUnit);
    
    int getSize();
    
    boolean isEmpty();
    
    //Events for addition and removal of gateways processing units
}
