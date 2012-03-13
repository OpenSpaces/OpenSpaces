package org.openspaces.admin.pu;

import java.util.Map;

import org.openspaces.events.EventContainerServiceMonitors;
import org.openspaces.events.asyncpolling.AsyncPollingEventContainerServiceMonitors;
import org.openspaces.events.notify.NotifyEventContainerServiceMonitors;
import org.openspaces.events.polling.PollingEventContainerServiceMonitors;
import org.openspaces.memcached.MemcachedServiceMonitors;
import org.openspaces.pu.container.jee.stats.WebRequestsServiceMonitors;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.remoting.RemotingServiceMonitors;

/**
 * Processing Unit Instance statistics provide statistics on services configured within the processing
 * unit. It uses the {@link org.openspaces.pu.service.ServiceMonitors} which each bean within a processing unit
 * can provide.
 *
 * @author kimchy
 */
public interface ProcessingUnitInstanceStatistics extends Iterable<ServiceMonitors> {

    /**
     * The timestamp the stats were taken at.
     */
    long getTimestamp();

    /**
     * Returns a timestamp that is in sync with where the admin API is running. Can return
     * -1 if the clocks have are not sync yet.
     */
    long getAdminTimestamp();

    /**
     * Returns a map of the {@link org.openspaces.pu.service.ServiceMonitors} per processing unit
     * service id (bean id or bean name).
     */
    Map<String, ServiceMonitors> getMonitors();

    /**
     * Returns all the different event containers monitors keyed by the service id.
     */
    Map<String, EventContainerServiceMonitors> getEventContainers();

    /**
     * Returns all the different polling event containers monitors keyed by the service id.
     */
    Map<String, PollingEventContainerServiceMonitors> getPollingEventContainers();

    /**
     * Returns all the different notify event containers monitors keyed by the service id.
     */
    Map<String, NotifyEventContainerServiceMonitors> getNotifyEventContainers();

    /**
     * Returns all the different async polling event containers monitors keyed by the service id.
     */
    Map<String, AsyncPollingEventContainerServiceMonitors> getAsyncPollingEventContainers();

    /**
     * Returns the remoting service (if configured) monitors infomration.
     */
    RemotingServiceMonitors getRemoting();

    /**
     * Returns statistics of JEE requests.
     */
    WebRequestsServiceMonitors getWebRequests();

    /**
     * Return memcached information.
     */
    MemcachedServiceMonitors getMemcached();

    /**
     * Returns the previous statistics.
     */
    ProcessingUnitInstanceStatistics getPrevious();

}
