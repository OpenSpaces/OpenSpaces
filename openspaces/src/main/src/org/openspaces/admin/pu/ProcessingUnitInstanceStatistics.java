package org.openspaces.admin.pu;

import org.openspaces.events.EventContainerServiceMonitors;
import org.openspaces.events.asyncpolling.AsyncPollingEventContainerServiceMonitors;
import org.openspaces.events.notify.NotifyEventContainerServiceMonitors;
import org.openspaces.events.polling.PollingEventContainerServiceMonitors;
import org.openspaces.pu.service.ServiceMonitors;

import java.util.Map;

/**
 * @author kimchy
 */
public interface ProcessingUnitInstanceStatistics extends Iterable<ServiceMonitors> {

    long getTimestamp();

    Map<String, ServiceMonitors> getMonitorsById();

    Map<String, EventContainerServiceMonitors> getEventContainerMonitorsById();

    Map<String, PollingEventContainerServiceMonitors> getPollingEventContainerMonitorsById();

    Map<String, NotifyEventContainerServiceMonitors> getNotifyEventContainerMonitorsById();

    Map<String, AsyncPollingEventContainerServiceMonitors> getAsyncPollingEventContainerMonitorsById();

    ProcessingUnitInstanceStatistics getPrevious();
}
