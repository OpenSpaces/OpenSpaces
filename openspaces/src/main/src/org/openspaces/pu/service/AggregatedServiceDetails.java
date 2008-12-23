package org.openspaces.pu.service;

import java.util.Map;

/**
 * An aggregation of {@link org.openspaces.pu.service.ServiceDetails} that share at lease the same
 * service type.
 *
 * @author kimchy
 */
public interface AggregatedServiceDetails {

    /**
     * Returns the aggregated service details.
     */
    String getServiceType();

    /**
     * Returns key value pair of attributes for the details.
     */
    Map<String, Object> getAttributes();
}
