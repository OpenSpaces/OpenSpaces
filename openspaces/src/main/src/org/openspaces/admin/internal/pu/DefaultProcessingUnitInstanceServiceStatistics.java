package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.events.EventContainerServiceMonitors;
import org.openspaces.events.asyncpolling.AsyncPollingEventContainerServiceMonitors;
import org.openspaces.events.notify.NotifyEventContainerServiceMonitors;
import org.openspaces.events.polling.PollingEventContainerServiceMonitors;
import org.openspaces.pu.container.jee.stats.WebRequestsServiceMonitors;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.remoting.RemotingServiceMonitors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitInstanceServiceStatistics implements ProcessingUnitInstanceStatistics {

    private final long timestamp;

    private final Map<String, ServiceMonitors> serviceMonitorsById;

    private volatile ProcessingUnitInstanceStatistics previous;

    private final Map<String, EventContainerServiceMonitors> eventContainerServiceMonitors = new HashMap<String, EventContainerServiceMonitors>();
    private final Map<String, PollingEventContainerServiceMonitors> pollingEventContainerServiceMonitors = new HashMap<String, PollingEventContainerServiceMonitors>();
    private final Map<String, NotifyEventContainerServiceMonitors> notifyEventContainerServiceMonitors = new HashMap<String, NotifyEventContainerServiceMonitors>();
    private final Map<String, AsyncPollingEventContainerServiceMonitors> asyncPollingEventContainerServiceMonitors = new HashMap<String, AsyncPollingEventContainerServiceMonitors>();

    private final RemotingServiceMonitors remotingServiceMonitors;
    private final WebRequestsServiceMonitors webRequestsServiceMonitors;

    public DefaultProcessingUnitInstanceServiceStatistics(long timestamp, Map<String, ServiceMonitors> serviceMonitorsById, ProcessingUnitInstanceStatistics previous,
                                                          int historySize) {
        this.timestamp = timestamp;
        this.serviceMonitorsById = serviceMonitorsById;
        this.previous = previous;
        RemotingServiceMonitors remotingServiceMonitorsX = null;
        WebRequestsServiceMonitors jeeRequestServiceMonitorsX = null;
        for (ServiceMonitors monitors : serviceMonitorsById.values()) {
            if (monitors instanceof EventContainerServiceMonitors) {
                eventContainerServiceMonitors.put(monitors.getId(), (EventContainerServiceMonitors) monitors);
                if (monitors instanceof PollingEventContainerServiceMonitors) {
                    pollingEventContainerServiceMonitors.put(monitors.getId(), (PollingEventContainerServiceMonitors) monitors);
                } else if (monitors instanceof NotifyEventContainerServiceMonitors) {
                    notifyEventContainerServiceMonitors.put(monitors.getId(), (NotifyEventContainerServiceMonitors) monitors);
                } else if (monitors instanceof AsyncPollingEventContainerServiceMonitors) {
                    asyncPollingEventContainerServiceMonitors.put(monitors.getId(), (AsyncPollingEventContainerServiceMonitors) monitors);
                }
            } else if (monitors instanceof RemotingServiceMonitors) {
                remotingServiceMonitorsX = (RemotingServiceMonitors) monitors;
            } else if (monitors instanceof WebRequestsServiceMonitors) {
                jeeRequestServiceMonitorsX = (WebRequestsServiceMonitors) monitors;
            }

        }
        this.remotingServiceMonitors = remotingServiceMonitorsX;
        this.webRequestsServiceMonitors = jeeRequestServiceMonitorsX;

        ProcessingUnitInstanceStatistics lastStats = null;
        for (int i = 0; i < historySize; i++) {
            if (getPrevious() == null) {
                lastStats = null;
                break;
            }
            lastStats = getPrevious();
        }
        if (lastStats != null) {
            ((DefaultProcessingUnitInstanceServiceStatistics) lastStats).setPrevious(null);
        }
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public Iterator<ServiceMonitors> iterator() {
        return serviceMonitorsById.values().iterator();
    }

    public Map<String, ServiceMonitors> getMonitors() {
        return this.serviceMonitorsById;
    }

    public Map<String, EventContainerServiceMonitors> getEventContainers() {
        return this.eventContainerServiceMonitors;
    }

    public Map<String, PollingEventContainerServiceMonitors> getPollingEventContainers() {
        return this.pollingEventContainerServiceMonitors;
    }

    public Map<String, NotifyEventContainerServiceMonitors> getNotifyEventContainers() {
        return this.notifyEventContainerServiceMonitors;
    }

    public Map<String, AsyncPollingEventContainerServiceMonitors> getAsyncPollingEventContainers() {
        return this.asyncPollingEventContainerServiceMonitors;
    }

    public RemotingServiceMonitors getRemoting() {
        return this.remotingServiceMonitors;
    }

    public WebRequestsServiceMonitors getWebRequests() {
        return this.webRequestsServiceMonitors;
    }

    public ProcessingUnitInstanceStatistics getPrevious() {
        return this.previous;
    }

    public void setPrevious(ProcessingUnitInstanceStatistics previous) {
        this.previous = previous;
    }
}
