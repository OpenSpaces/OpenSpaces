package org.openspaces.pu.service;

import java.io.Serializable;
import java.util.Map;

/**
 * A generic service that exists within a processing unit.
 *
 * @author kimchy
 * @see org.openspaces.pu.service.PlainServiceDetails
 */
public interface ServiceDetails extends Serializable {

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
    String getServiceSubType();

    /**
     * Returns a short description of the service.
     */
    String getDescription();

    /**
     * Returns the long description
     */
    String getLongDescription();

    /**
     * Returns extra atrributes the service details wishes to expose.
     */
    Map<String, Object> getAttributes();

    /**
     * Aggregates an array of service details into an aggregated view of it. All service details are of the same
     * service type. Can return <code>null</code> if no aggregation can be performed.
     */
    AggregatedServiceDetails aggregateByServiceType(ServiceDetails[] servicesDetails);

    /**
     * Aggregates an array of service details into an aggregated view of it. All service details are of the same
     * service type and service sub type. Can return <code>null</code> if no aggregation can be performed.
     */
    AggregatedServiceDetails aggregateByServiceSubType(ServiceDetails[] servicesDetails);

    /**
     * Aggregates an array of service details into an aggregated view of it. All service details are of the same
     * id (and service type and service sub type). Can return <code>null</code> if no aggregation can be performed.
     */
    AggregatedServiceDetails aggregateById(ServiceDetails[] servicesDetails);
}
