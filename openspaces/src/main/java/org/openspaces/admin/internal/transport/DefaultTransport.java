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
package org.openspaces.admin.internal.transport;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.transport.events.DefaultTransportStatisticsChangedEventManager;
import org.openspaces.admin.internal.transport.events.InternalTransportStatisticsChangedEventManager;
import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.transport.TransportDetails;
import org.openspaces.admin.transport.TransportLRMIMonitoring;
import org.openspaces.admin.transport.TransportStatistics;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEvent;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEventManager;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.core.util.ConcurrentHashSet;

import com.gigaspaces.lrmi.LRMIInboundMonitoringDetails;
import com.gigaspaces.lrmi.LRMIMonitoringDetails;
import com.gigaspaces.lrmi.LRMIOutboundMonitoringDetails;
import com.gigaspaces.lrmi.LRMIProxyMonitoringDetails;
import com.gigaspaces.lrmi.LRMIServiceMonitoringDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.j_spaces.kernel.time.SystemTime;

/**
 * @author kimchy
 */
public class DefaultTransport implements InternalTransport, TransportLRMIMonitoring {

    private final String uid;

    private final TransportDetails transportDetails;

    private final InternalTransports transports;

    private final InternalAdmin admin;

    private final Set<InternalTransportInfoProvider> transportInfoProviders = new ConcurrentHashSet<InternalTransportInfoProvider>();

    private volatile VirtualMachine virtualMachine;

    private long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private int statisticsHistorySize = StatisticsMonitor.DEFAULT_HISTORY_SIZE;

    private long lastStatisticsTimestamp = 0;

    private TransportStatistics lastStatistics;

    private Future scheduledStatisticsMonitor;
    
    private int scheduledStatisticsRefCount = 0;

    private final InternalTransportStatisticsChangedEventManager statisticsChangedEventManager;

    public DefaultTransport(NIODetails nioDetails, InternalTransports transports) {
        this.transportDetails = new DefaultTransportDetails(nioDetails);
        this.uid = getBindHost() + ":" + getPort();
        this.transports = transports;
        this.admin = (InternalAdmin) transports.getAdmin();

        this.statisticsChangedEventManager = new DefaultTransportStatisticsChangedEventManager(admin);
    }

    public TransportStatisticsChangedEventManager getStatisticsChanged() {
        return this.statisticsChangedEventManager;
    }

    public void addTransportInfoProvider(InternalTransportInfoProvider transportInfoProvider) {
        assertStateChangesPermitted();
        transportInfoProviders.add(transportInfoProvider);
    }

    public void removeTransportInfoProvider(InternalTransportInfoProvider transportInfoProvider) {
        assertStateChangesPermitted();
        transportInfoProviders.remove(transportInfoProvider);
    }

    public boolean hasTransportInfoProviders() {
        return !transportInfoProviders.isEmpty();
    }

    public String getUid() {
        return this.uid;
    }

    public String getHostAddress() {
        return transportDetails.getHostAddress();
    }

    public String getHostName() {
        return transportDetails.getHostName();
    }

    public String getBindHost() {
        return transportDetails.getBindHost();
    }

    public int getPort() {
        return transportDetails.getPort();
    }

    public TransportDetails getDetails() {
        return transportDetails;
    }

    public void setVirtualMachine(VirtualMachine virtualMachine) {
        assertStateChangesPermitted();
        this.virtualMachine = virtualMachine;
    }

    public VirtualMachine getVirtualMachine() {
        return this.virtualMachine;
    }

    private static final TransportStatistics NA_TRANSPORT_STATS = new DefaultTransportStatistics();

    public synchronized TransportStatistics getStatistics() {
        long currentTime = SystemTime.timeMillis();
        if ((currentTime - lastStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        TransportStatistics previousStats = lastStatistics;
        lastStatistics = NA_TRANSPORT_STATS;
        lastStatisticsTimestamp = currentTime;
        for (InternalTransportInfoProvider provider : transportInfoProviders) {
            try {
                if (getVirtualMachine().getMachine() == null) 
                    continue; //machine has not yet been set
                lastStatistics = new DefaultTransportStatistics(provider.getNIOStatistics(), previousStats, getDetails(), statisticsHistorySize, getVirtualMachine().getMachine().getOperatingSystem().getTimeDelta());
                break;
            } catch (RemoteException e) {
                // failed to get it, try next one
            }
        }
        return lastStatistics;
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        this.statisticsInterval = timeUnit.toMillis(interval);
        if (scheduledStatisticsMonitor != null) {
            stopStatisticsMonitor();
            startStatisticsMonitor();
        }
    }

    public synchronized void setStatisticsHistorySize(int historySize) {
        this.statisticsHistorySize = historySize;
    }

    public synchronized void startStatisticsMonitor() {
        if (scheduledStatisticsRefCount++ > 0) return;
        
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
        }
        final Transport transport = this;
        scheduledStatisticsMonitor = admin.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                TransportStatistics stats = transport.getStatistics();
                TransportStatisticsChangedEvent event = new TransportStatisticsChangedEvent(transport, stats);
                statisticsChangedEventManager.transportStatisticsChanged(event);
                ((InternalTransportStatisticsChangedEventManager) transports.getTransportStatisticsChanged()).transportStatisticsChanged(event);
            }
        }, 0, statisticsInterval, TimeUnit.MILLISECONDS);
    }

    public synchronized void stopStatisticsMonitor() {
        if (scheduledStatisticsRefCount!=0 && --scheduledStatisticsRefCount > 0) return;
        
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
            scheduledStatisticsMonitor = null;
        }
    }

    public synchronized boolean isMonitoring() {
        return scheduledStatisticsMonitor != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultTransport that = (DefaultTransport) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.transport.Transport#getDetailedMonitoring()
     */
    @Override
    public TransportLRMIMonitoring getLRMIMonitoring() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.transport.TransportMonitoring#enableMonitoring()
     */
    @Override
    public void enableMonitoring() {
        RemoteException lastException = null;
    	for (InternalTransportInfoProvider transportProvider : transportInfoProviders) {
            try {
            	transportProvider.enableLRMIMonitoring();
            	lastException = null;
                break;
            } catch (RemoteException e) {
                //Failure, try next one
                lastException = e;
            }
        }
    	if (lastException != null)
            throw new AdminException(lastException.getMessage(), lastException);
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.transport.TransportMonitoring#disableMonitoring()
     */
    @Override
    public void disableMonitoring() {
        RemoteException lastException = null;
    	for (InternalTransportInfoProvider transportProvider : transportInfoProviders) {
            try {
            	transportProvider.disableLRMIMonitoring();
            	lastException = null;
            	break;
            } catch (RemoteException e) {
                //Failure, try next one
                lastException = e;
            }
        }
    	if (lastException != null)
    	    throw new AdminException(lastException.getMessage(), lastException);
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.transport.TransportMonitoring#fetchMonitoringDetails()
     */
    @Override
    public synchronized LRMIMonitoringDetails fetchMonitoringDetails() {
        Exception lastException = null;
        for (InternalTransportInfoProvider transportProvider : transportInfoProviders) {
            try {
                return transportProvider.fetchLRMIMonitoringDetails();
            } catch (Exception e) {
                //Failure, try next one
                lastException = e;
            }
        }
        if (lastException != null)
            throw new AdminException(lastException.getMessage(), lastException);
        //If there are no available transports, create empty monitoring.
        return new LRMIMonitoringDetails() {
            @Override
            public LRMIOutboundMonitoringDetails getOutboundMonitoringDetails() {
                return new LRMIOutboundMonitoringDetails() {
                    @Override
                    public LRMIProxyMonitoringDetails[] getProxiesMonitoringDetails() {
                        return new LRMIProxyMonitoringDetails[0];
                    }
                };
            }
            
            @Override
            public LRMIInboundMonitoringDetails getInboundMonitoringDetails() {
                return new LRMIInboundMonitoringDetails() {
                    @Override
                    public LRMIServiceMonitoringDetails[] getServicesMonitoringDetails() {
                        return new LRMIServiceMonitoringDetails[0];
                    }
                };
            }
        };
    }

}
