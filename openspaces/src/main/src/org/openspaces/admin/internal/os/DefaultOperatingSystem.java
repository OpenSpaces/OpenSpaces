package org.openspaces.admin.internal.os;

import com.gigaspaces.operatingsystem.OSDetails;
import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.os.events.DefaultOperatingSystemStatisticsChangedEventManager;
import org.openspaces.admin.internal.os.events.InternalOperatingSystemStatisticsChangedEventManager;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEvent;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventManager;
import org.openspaces.core.util.ConcurrentHashSet;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultOperatingSystem implements InternalOperatingSystem {

    private final String uid;

    private final OperatingSystemDetails details;

    private final InternalAdmin admin;

    private final InternalOperatingSystems operatingSystems;

    private final Set<InternalOperatingSystemInfoProvider> operatingSystemInfoProviders = new ConcurrentHashSet<InternalOperatingSystemInfoProvider>();

    private final InternalOperatingSystemStatisticsChangedEventManager statisticsChangedEventManager;

    private long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private long lastStatisticsTimestamp = 0;

    private OperatingSystemStatistics lastStatistics;

    private Future scheduledStatisticsMonitor;

    public DefaultOperatingSystem(OSDetails osDetails, InternalOperatingSystems operatingSystems) {
        this.details = new DefaultOperatingSystemDetails(osDetails);
        this.uid = details.getUid();
        this.operatingSystems = operatingSystems;
        if (operatingSystems != null) {
            this.admin = (InternalAdmin) operatingSystems.getAdmin();
        } else {
            this.admin = null;
        }

        this.statisticsChangedEventManager = new DefaultOperatingSystemStatisticsChangedEventManager(admin);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public void addOperatingSystemInfoProvider(InternalOperatingSystemInfoProvider provider) {
        operatingSystemInfoProviders.add(provider);
    }

    public void removeOperatingSystemInfoProvider(InternalOperatingSystemInfoProvider provider) {
        operatingSystemInfoProviders.remove(provider);
    }

    public boolean hasOperatingSystemInfoProviders() {
        return !operatingSystemInfoProviders.isEmpty();
    }

    public String getUid() {
        return this.uid;
    }

    public OperatingSystemDetails getDetails() {
        return this.details;
    }

    private static final OperatingSystemStatistics NA_STATS = new DefaultOperatingSystemStatistics();

    public synchronized OperatingSystemStatistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        OperatingSystemStatistics previousStats = lastStatistics;
        lastStatistics = NA_STATS;
        lastStatisticsTimestamp = currentTime;
        for (InternalOperatingSystemInfoProvider provider : operatingSystemInfoProviders) {
            try {
                lastStatistics = new DefaultOperatingSystemStatistics(provider.getOSStatistics(), getDetails(), previousStats);
                break;
            } catch (RemoteException e) {
                // simply try the next one
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

    public synchronized void startStatisticsMonitor() {
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
        }
        final OperatingSystem operatingSystem = this;
        scheduledStatisticsMonitor = admin.getScheduler().scheduleWithFixedDelay(new Runnable() {
            public void run() {
                OperatingSystemStatistics stats = operatingSystem.getStatistics();
                OperatingSystemStatisticsChangedEvent event = new OperatingSystemStatisticsChangedEvent(operatingSystem, stats);
                statisticsChangedEventManager.operatingSystemStatisticsChanged(event);
                ((InternalOperatingSystemStatisticsChangedEventManager) operatingSystems.getOperatingSystemStatisticsChanged()).operatingSystemStatisticsChanged(event);
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


    public OperatingSystemStatisticsChangedEventManager getStatisticsChanged() {
        return this.statisticsChangedEventManager;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultOperatingSystem that = (DefaultOperatingSystem) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
}
