package org.openspaces.admin.internal.transport;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.transport.events.DefaultTransportStatisticsChangedEventManager;
import org.openspaces.admin.internal.transport.events.DefaultTransportsStatisticsChangedEventManager;
import org.openspaces.admin.internal.transport.events.InternalTransportStatisticsChangedEventManager;
import org.openspaces.admin.internal.transport.events.InternalTransportsStatisticsChangedEventManager;
import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.transport.TransportDetails;
import org.openspaces.admin.transport.TransportStatistics;
import org.openspaces.admin.transport.TransportsDetails;
import org.openspaces.admin.transport.TransportsStatistics;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEventManager;
import org.openspaces.admin.transport.events.TransportsStatisticsChangedEvent;
import org.openspaces.admin.transport.events.TransportsStatisticsChangedEventManager;
import org.openspaces.core.util.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultTransports implements InternalTransports {

    private final InternalAdmin admin;

    private final Map<String, Transport> transportsByUID = new SizeConcurrentHashMap<String, Transport>();

    private final Map<String, Set<Transport>> transportsByHost = new SizeConcurrentHashMap<String, Set<Transport>>();

    private final InternalTransportStatisticsChangedEventManager transportStatisticsChangedEventManager;

    private final InternalTransportsStatisticsChangedEventManager transportsStatisticsChangedEventManager;

    private long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private int statisticsHistorySize = StatisticsMonitor.DEFAULT_HISTORY_SIZE;

    private long lastStatisticsTimestamp = 0;

    private TransportsStatistics lastStatistics;

    private Future scheduledStatisticsMonitor;

    public DefaultTransports(InternalAdmin admin) {
        this.admin = admin;
        this.transportStatisticsChangedEventManager = new DefaultTransportStatisticsChangedEventManager(admin);
        this.transportsStatisticsChangedEventManager = new DefaultTransportsStatisticsChangedEventManager(admin);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public TransportStatisticsChangedEventManager getTransportStatisticsChanged() {
        return this.transportStatisticsChangedEventManager;
    }

    public TransportsStatisticsChangedEventManager getStatisticsChanged() {
        return this.transportsStatisticsChangedEventManager;
    }

    public Transport[] getTransports() {
        return transportsByUID.values().toArray(new Transport[0]);
    }

    public Iterator<Transport> iterator() {
        return transportsByUID.values().iterator();
    }

    public Transport[] getTransports(String host) {
        Set<Transport> transportByHost = transportsByHost.get(host);
        if (transportByHost == null) {
            return new Transport[0];
        }
        return transportByHost.toArray(new Transport[0]);
    }

    public Transport getTransportByHostAndPort(String host, int port) {
        return transportsByUID.get(host + ":" + port);
    }

    public Transport getTransportByUID(String uid) {
        return transportsByUID.get(uid);
    }

    public int getSize() {
        return transportsByUID.size();
    }

    public TransportsDetails getDetails() {
        List<TransportDetails> details = new ArrayList<TransportDetails>();
        for (Transport transport : transportsByUID.values()) {
            details.add(transport.getDetails());
        }
        return new DefaultTransportsDetails(details.toArray(new TransportDetails[details.size()]));
    }

    public synchronized TransportsStatistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        lastStatisticsTimestamp = currentTime;
        List<TransportStatistics> stats = new ArrayList<TransportStatistics>();
        List<TransportDetails> details = new ArrayList<TransportDetails>();
        for (Transport transport : transportsByUID.values()) {
            stats.add(transport.getStatistics());
            details.add(transport.getDetails());
        }
        lastStatistics = new DefaultTransportsStatistics(stats.toArray(new TransportStatistics[stats.size()]), lastStatistics,
                new DefaultTransportsDetails(details.toArray(new TransportDetails[details.size()])), statisticsHistorySize);
        return lastStatistics;
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        statisticsInterval = timeUnit.toMillis(interval);
        if (isMonitoring()) {
            rescheduleStatisticsMonitor();
        }
        for (Transport transport : transportsByUID.values()) {
            transport.setStatisticsInterval(interval, timeUnit);
        }
    }

    public synchronized void setStatisticsHistorySize(int historySize) {
        this.statisticsHistorySize = historySize;
        for (Transport transport : transportsByUID.values()) {
            transport.setStatisticsHistorySize(historySize);
        }
    }

    public synchronized void startStatisticsMonitor() {
        rescheduleStatisticsMonitor();
        for (Transport transport : transportsByUID.values()) {
            transport.startStatisticsMonitor();
        }
    }

    public synchronized void stopStatisticsMontior() {
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
            scheduledStatisticsMonitor = null;
        }
        for (Transport transport : transportsByUID.values()) {
            transport.stopStatisticsMontior();
        }
    }

    public synchronized boolean isMonitoring() {
        return scheduledStatisticsMonitor != null;
    }

    private void rescheduleStatisticsMonitor() {
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
        }
        scheduledStatisticsMonitor = admin.getScheduler().scheduleWithFixedDelay(new Runnable() {
            public void run() {
                TransportsStatistics stats = getStatistics();
                TransportsStatisticsChangedEvent event = new TransportsStatisticsChangedEvent(DefaultTransports.this, stats);
                transportsStatisticsChangedEventManager.transportsStatisticsChanged(event);
            }
        }, 0, statisticsInterval, TimeUnit.MILLISECONDS);
    }


    public void addTransport(Transport transport) {
        Transport existingTransport = transportsByUID.put(transport.getUid(), transport);
        if (existingTransport == null) {
            // a new one, set the stats on it
            transport.setStatisticsInterval(statisticsInterval, TimeUnit.MILLISECONDS);
            transport.setStatisticsHistorySize(statisticsHistorySize);
            if (isMonitoring()) {
                transport.startStatisticsMonitor();
            }
        }
        Set<Transport> transportByHost = transportsByHost.get(transport.getBindHost());
        if (transportByHost == null) {
            synchronized (transportsByHost) {
                transportByHost = transportsByHost.get(transport.getBindHost());
                if (transportByHost == null) {
                    transportByHost = new ConcurrentHashSet<Transport>();
                    transportsByHost.put(transport.getBindHost(), transportByHost);
                }
            }
        }
        transportByHost.add(transport);
    }

    public void removeTransport(String uid) {
        Transport transport = transportsByUID.remove(uid);
        if (transport == null) {
            return;
        }
        transport.stopStatisticsMontior();

        Set<Transport> transportByHost = transportsByHost.get(transport.getBindHost());
        if (transportByHost == null) {
            synchronized (transportsByHost) {
                transportByHost = transportsByHost.get(transport.getBindHost());
                if (transportByHost != null) {
                    transportByHost.remove(transport);
                    if (transportByHost.isEmpty()) {
                        transportsByHost.remove(transport.getBindHost());
                    }
                }
            }
        } else {
            transportByHost.remove(transport);
        }
    }
}
