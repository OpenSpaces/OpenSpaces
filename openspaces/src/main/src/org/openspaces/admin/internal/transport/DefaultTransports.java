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
import org.openspaces.admin.transport.TransportStatistics;
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

    private volatile long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

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

    public int size() {
        return transportsByUID.size();
    }

    public TransportsStatistics getStatistics() {
        List<TransportStatistics> stats = new ArrayList<TransportStatistics>();
        for (Transport transport : transportsByUID.values()) {
            stats.add(transport.getStatistics());
        }
        return new DefaultTransportsStatistics(stats.toArray(new TransportStatistics[stats.size()]));
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        statisticsInterval = timeUnit.toMillis(interval);
        if (isMonitoring()) {
            rescheduleStatisticsMonitor();
        }
        rescheduleStatisticsMonitor();
        for (Transport transport : transportsByUID.values()) {
            transport.setStatisticsInterval(interval, timeUnit);
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
        transportsByUID.put(transport.getUid(), transport);
        Set<Transport> transportByHost = transportsByHost.get(transport.getHost());
        if (transportByHost == null) {
            if (isMonitoring()) {
                transport.startStatisticsMonitor();
            }
            synchronized (transportsByHost) {
                transportByHost = transportsByHost.get(transport.getHost());
                if (transportByHost == null) {
                    transportByHost = new ConcurrentHashSet<Transport>();
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

        Set<Transport> transportByHost = transportsByHost.get(transport.getHost());
        if (transportByHost == null) {
            synchronized (transportsByHost) {
                transportByHost = transportsByHost.get(transport.getHost());
                if (transportByHost != null) {
                    transportByHost.remove(transport);
                }
            }
        } else {
            transportByHost.remove(transport);
        }
    }
}
