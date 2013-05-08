/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.pu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.events.EventContainerServiceMonitors;
import org.openspaces.events.asyncpolling.AsyncPollingEventContainerServiceMonitors;
import org.openspaces.events.notify.NotifyEventContainerServiceMonitors;
import org.openspaces.events.polling.PollingEventContainerServiceMonitors;
import org.openspaces.memcached.MemcachedServiceMonitors;
import org.openspaces.pu.container.jee.stats.WebRequestsServiceMonitors;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.remoting.RemotingServiceMonitors;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitInstanceServiceStatistics implements ProcessingUnitInstanceStatistics {

    private final long timeDelta;

    private final long timestamp;

    private final Map<String, ServiceMonitors> serviceMonitorsById;

    private volatile ProcessingUnitInstanceStatistics previous;

    public DefaultProcessingUnitInstanceServiceStatistics(long timestamp, Map<String, ServiceMonitors> serviceMonitorsById, ProcessingUnitInstanceStatistics previous,
                                                          int historySize, long timeDelta) {
        this.timestamp = timestamp;
        this.timeDelta = timeDelta;
        this.previous = previous;
        
        this.serviceMonitorsById = new HashMap<String,ServiceMonitors>(serviceMonitorsById);
        
        ProcessingUnitInstanceStatistics lastStats = previous;
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultProcessingUnitInstanceServiceStatistics) lastStats).setPrevious(null);
        }

        WebRequestsServiceMonitors webRequests = getWebRequests();
        if( webRequests != null ){
            if (previous != null) {
                webRequests.setPrevious( previous.getWebRequests(), 
                        getAdminTimestamp() - previous.getAdminTimestamp() );
            }
            else{
                webRequests.setPrevious( null, 0 ); 
            }
        }
    }
   
    
    public long getTimestamp() {
        return this.timestamp;
    }

    public long getAdminTimestamp() {
        if (timestamp != -1 && timeDelta != Integer.MIN_VALUE) {
            return timestamp + timeDelta;
        }
        return -1;
    }

    public Iterator<ServiceMonitors> iterator() {
        return Collections.unmodifiableCollection(serviceMonitorsById.values()).iterator();
    }

    public Map<String, ServiceMonitors> getMonitors() {
        return Collections.unmodifiableMap(this.serviceMonitorsById);
    }

    public Map<String, EventContainerServiceMonitors> getEventContainers() {
        Map<String, EventContainerServiceMonitors> eventContainerServiceMonitors = new HashMap<String, EventContainerServiceMonitors>();
        for (ServiceMonitors monitors : serviceMonitorsById.values()) {
            if (monitors instanceof EventContainerServiceMonitors) {
                if (eventContainerServiceMonitors == Collections.EMPTY_MAP) {
                    eventContainerServiceMonitors = new HashMap<String, EventContainerServiceMonitors>();
                }
                eventContainerServiceMonitors.put(monitors.getId(), (EventContainerServiceMonitors) monitors);
            }
        }
        return eventContainerServiceMonitors;
    }

    public Map<String, PollingEventContainerServiceMonitors> getPollingEventContainers() {
        Map<String, PollingEventContainerServiceMonitors> pollingEventContainerServiceMonitors = new HashMap<String, PollingEventContainerServiceMonitors>();
        for (ServiceMonitors monitors : serviceMonitorsById.values()) {
            if (monitors instanceof EventContainerServiceMonitors) {
                if (monitors instanceof PollingEventContainerServiceMonitors) {
                    if (pollingEventContainerServiceMonitors == Collections.EMPTY_MAP) {
                        pollingEventContainerServiceMonitors = new HashMap<String, PollingEventContainerServiceMonitors>();
                    }
                    pollingEventContainerServiceMonitors.put(monitors.getId(), (PollingEventContainerServiceMonitors) monitors);
                }
            }
        }
        return pollingEventContainerServiceMonitors;
    }

    public Map<String, NotifyEventContainerServiceMonitors> getNotifyEventContainers() {
        Map<String, NotifyEventContainerServiceMonitors> notifyEventContainerServiceMonitors = new HashMap<String, NotifyEventContainerServiceMonitors>();
        for (ServiceMonitors monitors : serviceMonitorsById.values()) {
            if (monitors instanceof EventContainerServiceMonitors) {
                if (monitors instanceof NotifyEventContainerServiceMonitors) {
                    if (notifyEventContainerServiceMonitors == Collections.EMPTY_MAP) {
                        notifyEventContainerServiceMonitors = new HashMap<String, NotifyEventContainerServiceMonitors>();
                    }
                    notifyEventContainerServiceMonitors.put(monitors.getId(), (NotifyEventContainerServiceMonitors) monitors);
                }
            }
        }
        return notifyEventContainerServiceMonitors;
    }

    public Map<String, AsyncPollingEventContainerServiceMonitors> getAsyncPollingEventContainers() {
        Map<String, AsyncPollingEventContainerServiceMonitors> asyncPollingEventContainerServiceMonitors = new HashMap<String, AsyncPollingEventContainerServiceMonitors>();
        for (ServiceMonitors monitors : serviceMonitorsById.values()) {
            if (monitors instanceof EventContainerServiceMonitors) {
                if (monitors instanceof AsyncPollingEventContainerServiceMonitors) {
                    if (asyncPollingEventContainerServiceMonitors == Collections.EMPTY_MAP) {
                        asyncPollingEventContainerServiceMonitors = new HashMap<String, AsyncPollingEventContainerServiceMonitors>();
                    }
                    asyncPollingEventContainerServiceMonitors.put(monitors.getId(), (AsyncPollingEventContainerServiceMonitors) monitors);
                }
            }
        }
        return asyncPollingEventContainerServiceMonitors;
    }

    public RemotingServiceMonitors getRemoting() {
        for (ServiceMonitors monitors : serviceMonitorsById.values()) {
            if (monitors instanceof RemotingServiceMonitors) {
                return (RemotingServiceMonitors) monitors;
            }
        }
        return null;
    }

    public WebRequestsServiceMonitors getWebRequests() {
        for (ServiceMonitors monitors : serviceMonitorsById.values()) {
            if (monitors instanceof WebRequestsServiceMonitors) {
                return (WebRequestsServiceMonitors) monitors;
            }
        }
        return null;
    }

    public MemcachedServiceMonitors getMemcached() {
        for (ServiceMonitors monitors : serviceMonitorsById.values()) {
            if (monitors instanceof MemcachedServiceMonitors) {
                return (MemcachedServiceMonitors) monitors;
            }
        }
        return null;
    }

    public ProcessingUnitInstanceStatistics getPrevious() {
        return this.previous;
    }

    public void setPrevious(ProcessingUnitInstanceStatistics previous) {
        this.previous = previous;
    }


    public void addMonitors(ServiceMonitors[] monitors) {
        for (ServiceMonitors serviceMonitors : monitors) {
            serviceMonitorsById.put(serviceMonitors.getId(), serviceMonitors);
        }
    }

	@Override
	public List<ProcessingUnitInstanceStatistics> getTimelineFromTimestamp( long fromTimestamp ) {
        List<ProcessingUnitInstanceStatistics> timeline = new ArrayList<ProcessingUnitInstanceStatistics>();
        
        ProcessingUnitInstanceStatistics current = this;
        while( current != null && current.getTimestamp() > fromTimestamp ) {
            timeline.add(current);
            current = current.getPrevious();
        }
        return timeline;
	}
}
