package org.openspaces.admin.gateway;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminAware;

/**
 * Gateways holds all the different {@link Gateway}s that are currently
 * discovered.
 * 
 * @author eitany
 * @since 8.0.3
 */
public interface Gateways extends AdminAware, Iterable<Gateway> {
    
    /**
     * Returns all the currently discovered gateways. 
     */
    Gateway[] getGateways();
    
    /**
     * Returns the {@link Gateway} for the given processing unit name. Returns <code>null</code> if the processing unit
     * is not currently discovered or not hosting a {@link Gateway}.
     */
    Gateway getGateway(String hostingProcessingUnitName);
    
    /**
     * Returns a map of {@link Gateway} keyed by their respective hosting processing unit names.
     */
    Map<String, Gateway> getNames();
    
    /**
     * Waits for the default timeout specified by {@link Admin#setDefaultTimeout(long, TimeUnit)} till the gateway hosted in the specified processing unit is identified as deployed. 
     * Return <code>null</code> if the hosting processing unit is not deployed
     * within the specified timeout or not hosting a {@link Gateway}.
     */
    Gateway waitFor(String hostingProcessingUnitName);

    /**
     * Waits for the specified timeout (in time interval) till the gateway hosted in the specified processing unit is identified as deployed. Returns the
     * {@link Gateway}. Return <code>null</code> if the hosting processing unit is not deployed
     * within the specified timeout or not hosting a {@link Gateway}.
     */
    Gateway waitFor(String hostingProcessingUnitName, long timeout, TimeUnit timeUnit);
    
    /**
     * Returns the number of gateways currently discovered.
     */
    int getSize();
    
    /**
     * Returns <code>true</code> if there are no gateways, <code>false</code> otherwise.
     */
    boolean isEmpty();
}
