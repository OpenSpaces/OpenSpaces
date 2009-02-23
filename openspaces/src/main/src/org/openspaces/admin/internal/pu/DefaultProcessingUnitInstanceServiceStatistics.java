package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.events.EventContainerServiceMonitors;
import org.openspaces.events.asyncpolling.AsyncPollingEventContainerServiceMonitors;
import org.openspaces.events.notify.NotifyEventContainerServiceMonitors;
import org.openspaces.events.polling.PollingEventContainerServiceMonitors;
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

    private final ProcessingUnitInstanceStatistics previous;

    private final Map<String, EventContainerServiceMonitors> eventContainerServiceMonitors = new HashMap<String, EventContainerServiceMonitors>();
    private final Map<String, PollingEventContainerServiceMonitors> pollingEventContainerServiceMonitors = new HashMap<String, PollingEventContainerServiceMonitors>();
    private final Map<String, NotifyEventContainerServiceMonitors> notifyEventContainerServiceMonitors = new HashMap<String, NotifyEventContainerServiceMonitors>();
    private final Map<String, AsyncPollingEventContainerServiceMonitors> asyncPollingEventContainerServiceMonitors = new HashMap<String, AsyncPollingEventContainerServiceMonitors>();

    private final RemotingServiceMonitors remotingServiceMonitors;

    public DefaultProcessingUnitInstanceServiceStatistics(long timestamp, Map<String, ServiceMonitors> serviceMonitorsById, ProcessingUnitInstanceStatistics previous) {
        this.timestamp = timestamp;
        this.serviceMonitorsById = serviceMonitorsById;
        this.previous = previous;
        RemotingServiceMonitors remotingServiceMonitorsX = null;
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
            }

        }
        this.remotingServiceMonitors = remotingServiceMonitorsX;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public Iterator<ServiceMonitors> iterator() {
        return serviceMonitorsById.values().iterator();
    }

    public Map<String, ServiceMonitors> getMonitorsById() {
        return this.serviceMonitorsById;
    }

    public Map<String, EventContainerServiceMonitors> getEventContainerMonitorsById() {
        return this.eventContainerServiceMonitors;
    }

    public Map<String, PollingEventContainerServiceMonitors> getPollingEventContainerMonitorsById() {
        return this.pollingEventContainerServiceMonitors;
    }

    public Map<String, NotifyEventContainerServiceMonitors> getNotifyEventContainerMonitorsById() {
        return this.notifyEventContainerServiceMonitors;
    }

    public Map<String, AsyncPollingEventContainerServiceMonitors> getAsyncPollingEventContainerMonitorsById() {
        return this.asyncPollingEventContainerServiceMonitors;
    }

    public RemotingServiceMonitors getRemotingServiceMonitors() {
        return this.remotingServiceMonitors;
    }

    public ProcessingUnitInstanceStatistics getPrevious() {
        return this.previous;
    }
}
