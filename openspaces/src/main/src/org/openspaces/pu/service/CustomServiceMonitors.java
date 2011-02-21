package org.openspaces.pu.service;


/**
 * This class should be used by customers in order to implement their ServiceMonitors
 *
 * @since 8.0.1
 */
public class CustomServiceMonitors extends PlainServiceMonitors {

    public CustomServiceMonitors() {
    }

    /**
     * 
     * @param id should identify that service
     */
    public CustomServiceMonitors(String id) {
        super(id);
    }
    
}