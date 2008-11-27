package org.openspaces.pu.service;

import java.io.Serializable;

/**
 * A generic service that exists within a processing unit.
 *
 * @author kimchy
 */
public interface ProcessingUnitServiceDetails extends Serializable {

    /**
     * Returns the id of the processing unit (usually the bean id).
     */
    String getId();

    /**
     * Returns the service type. For example, space, dotnet, jee.
     */
    String getServiceType();

    /**
     * Returns the type of the serive details. For example, in case of
     * space, it can be localcache, proxy, ... .
     */
    String getType();

    /**
     * Returns a short description of the service.
     */
    String getDescription();

    /**
     * Returns the long description
     */
    String getLongDescription();
}
