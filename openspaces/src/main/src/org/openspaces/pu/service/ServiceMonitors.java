package org.openspaces.pu.service;

import java.io.Serializable;
import java.util.Map;

/**
 * A generic service that exists within a processing unit.
 *
 * @author kimchy
 * @see org.openspaces.pu.service.PlainServiceMonitors
 */
public interface ServiceMonitors extends Serializable {

    /**
     * Returns the id of the service monitor (usually the bean id).
     */
    String getId();

    /**
     * Returns the details of the service.
     *
     * <p>Note, should not be marshalled from the server to the client, the client should be able to set it.
     */
    ServiceDetails getDetails();

    /**
     * Returns monitor values.
     */
    Map<String, Object> getMonitors();
}