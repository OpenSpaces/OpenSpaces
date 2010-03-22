package org.openspaces.admin.internal.transport;

import com.gigaspaces.lrmi.nio.info.NIODetails;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.transport.events.DefaultTransportStatisticsChangedEventManager;
import org.openspaces.admin.internal.transport.events.InternalTransportStatisticsChangedEventManager;
import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.transport.TransportDetails;
import org.openspaces.admin.transport.TransportStatistics;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEvent;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEventManager;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.core.util.ConcurrentHashSet;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultTransport implements InternalTransport {

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
        transportInfoProviders.add(transportInfoProvider);
    }

    public void removeTransportInfoProvider(InternalTransportInfoProvider transportInfoProvider) {
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
        this.virtualMachine = virtualMachine;
    }

    public VirtualMachine getVirtualMachine() {
        return this.virtualMachine;
    }

    private static final TransportStatistics NA_TRANSPORT_STATS = new DefaultTransportStatistics();

    public synchronized TransportStatistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        TransportStatistics previousStats = lastStatistics;
        lastStatistics = NA_TRANSPORT_STATS;
        lastStatisticsTimestamp = currentTime;
        for (InternalTransportInfoProvider provider : transportInfoProviders) {
            try {
                lastStatistics = new DefaultTransportStatistics(provider.getNIOStatistics(), previousStats, getDetails(), statisticsHistorySize, getVirtualMachine().getMachine().getOperatingSystem().getTimeDelta());
            } catch (RemoteException e) {
                // failed to get it, try next one
            }
        }
        return lastStatistics;
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        this.statisticsInterval = timeUnit.toMillis(interval);
        if (scheduledStatisticsMonitor != null) {
            stopStatisticsMontior();
            startStatisticsMonitor();
        }
    }

    public synchronized void setStatisticsHistorySize(int historySize) {
        this.statisticsHistorySize = historySize;
    }

    public synchronized void startStatisticsMonitor() {
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
        }
        final Transport transport = this;
        scheduledStatisticsMonitor = admin.getScheduler().scheduleWithFixedDelay(new Runnable() {
            public void run() {
                TransportStatistics stats = transport.getStatistics();
                TransportStatisticsChangedEvent event = new TransportStatisticsChangedEvent(transport, stats);
                statisticsChangedEventManager.transportStatisticsChanged(event);
                ((InternalTransportStatisticsChangedEventManager) transports.getTransportStatisticsChanged()).transportStatisticsChanged(event);
            }
        }, 0, statisticsInterval, TimeUnit.MILLISECONDS);
    }

    public synchronized void stopStatisticsMontior() {
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
}
