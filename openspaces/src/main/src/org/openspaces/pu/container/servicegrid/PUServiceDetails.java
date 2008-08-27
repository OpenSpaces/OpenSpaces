package org.openspaces.pu.container.servicegrid;

import java.io.Serializable;

/**
 * A generic service that exists within a processing unit.
 *
 * @author kimchy
 */
public interface PUServiceDetails extends Serializable {

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
}
