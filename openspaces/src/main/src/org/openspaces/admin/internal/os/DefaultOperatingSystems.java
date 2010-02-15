package org.openspaces.admin.internal.os;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.os.events.DefaultOperatingSystemStatisticsChangedEventManager;
import org.openspaces.admin.internal.os.events.DefaultOperatingSystemsStatisticsChangedEventManager;
import org.openspaces.admin.internal.os.events.InternalOperatingSystemStatisticsChangedEventManager;
import org.openspaces.admin.internal.os.events.InternalOperatingSystemsStatisticsChangedEventManager;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.os.OperatingSystemsDetails;
import org.openspaces.admin.os.OperatingSystemsStatistics;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventManager;
import org.openspaces.admin.os.events.OperatingSystemsStatisticsChangedEvent;
import org.openspaces.admin.os.events.OperatingSystemsStatisticsChangedEventManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultOperatingSystems implements InternalOperatingSystems {

    private final InternalAdmin admin;

    private final Map<String, OperatingSystem> operatingSystemsByUID = new SizeConcurrentHashMap<String, OperatingSystem>();

    private final InternalOperatingSystemsStatisticsChangedEventManager statisticsChangedEventManager;

    private final InternalOperatingSystemStatisticsChangedEventManager operatingSystemStatisticsChangedEventManager;

    private volatile long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private volatile int statisticsHistorySize = StatisticsMonitor.DEFAULT_HISTORY_SIZE;

    private long lastStatisticsTimestamp = 0;

    private OperatingSystemsStatistics lastStatistics;

    private Future scheduledStatisticsMonitor;

    public DefaultOperatingSystems(InternalAdmin admin) {
        this.admin = admin;

        this.statisticsChangedEventManager = new DefaultOperatingSystemsStatisticsChangedEventManager(admin, this);
        this.operatingSystemStatisticsChangedEventManager = new DefaultOperatingSystemStatisticsChangedEventManager(admin, this);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public OperatingSystem[] getOperatingSystems() {
        return operatingSystemsByUID.values().toArray(new OperatingSystem[0]);
    }

    public Iterator<OperatingSystem> iterator() {
        return operatingSystemsByUID.values().iterator();
    }

    public int getSize() {
        return operatingSystemsByUID.size();
    }

    public OperatingSystemsDetails getDetails() {
        List<OperatingSystemDetails> details = new ArrayList<OperatingSystemDetails>();
        for (OperatingSystem os : operatingSystemsByUID.values()) {
            details.add(os.getDetails());
        }
        return new DefaultOperatingSystemsDetails(details.toArray(new OperatingSystemDetails[details.size()]));
    }

    public synchronized OperatingSystemsStatistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        lastStatisticsTimestamp = currentTime;
        List<OperatingSystemStatistics> stats = new ArrayList<OperatingSystemStatistics>();
        List<OperatingSystemDetails> details = new ArrayList<OperatingSystemDetails>();
        for (OperatingSystem os : operatingSystemsByUID.values()) {
            stats.add(os.getStatistics());
            details.add(os.getDetails());
        }
        lastStatistics = new DefaultOperatingSystemsStatistics(stats.toArray(new OperatingSystemStatistics[stats.size()]),
                lastStatistics, new DefaultOperatingSystemsDetails(details.toArray(new OperatingSystemDetails[details.size()])), statisticsHistorySize);
        return lastStatistics;
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        statisticsInterval = timeUnit.toMillis(interval);
        if (isMonitoring()) {
            rescheduleStatisticsMonitor();
        }
        for (OperatingSystem operatingSystem : operatingSystemsByUID.values()) {
            operatingSystem.setStatisticsInterval(interval, timeUnit);
        }
    }

    public void setStatisticsHistorySize(int historySize) {
        this.statisticsHistorySize = historySize;
        for (OperatingSystem operatingSystem : operatingSystemsByUID.values()) {
            operatingSystem.setStatisticsHistorySize(statisticsHistorySize);
        }
    }

    public synchronized void startStatisticsMonitor() {
        rescheduleStatisticsMonitor();
        for (OperatingSystem operatingSystem : operatingSystemsByUID.values()) {
            operatingSystem.startStatisticsMonitor();
        }
    }

    public synchronized void stopStatisticsMontior() {
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
            scheduledStatisticsMonitor = null;
        }
        for (OperatingSystem operatingSystem : operatingSystemsByUID.values()) {
            operatingSystem.stopStatisticsMontior();
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
                OperatingSystemsStatistics stats = getStatistics();
                OperatingSystemsStatisticsChangedEvent event = new OperatingSystemsStatisticsChangedEvent(DefaultOperatingSystems.this, stats);
                statisticsChangedEventManager.operatingSystemsStatisticsChanged(event);
            }
        }, 0, statisticsInterval, TimeUnit.MILLISECONDS);
    }

    public OperatingSystemsStatisticsChangedEventManager getStatisticsChanged() {
        return this.statisticsChangedEventManager;
    }

    public OperatingSystemStatisticsChangedEventManager getOperatingSystemStatisticsChanged() {
        return this.operatingSystemStatisticsChangedEventManager;
    }

    public OperatingSystem getByUID(String uid) {
        return operatingSystemsByUID.get(uid);
    }

    public Map<String, OperatingSystem> getUids() {
        return Collections.unmodifiableMap(operatingSystemsByUID);
    }

    public void addOperatingSystem(OperatingSystem operatingSystem) {
        OperatingSystem existing = operatingSystemsByUID.put(operatingSystem.getUid(), operatingSystem);
        if (existing == null) {
            operatingSystem.setStatisticsHistorySize(statisticsHistorySize);
            operatingSystem.setStatisticsInterval(statisticsInterval, TimeUnit.MILLISECONDS);
            if (isMonitoring()) {
                operatingSystem.startStatisticsMonitor();
            }
        }
    }

    public void removeOperatingSystem(String uid) {
        OperatingSystem existing = operatingSystemsByUID.remove(uid);
        if (existing != null) {
            existing.stopStatisticsMontior();
        }
    }
}
