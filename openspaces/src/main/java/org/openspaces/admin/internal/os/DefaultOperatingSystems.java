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
    
    private int scheduledStatisticsRefCount = 0;

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
        return Collections.unmodifiableCollection(operatingSystemsByUID.values()).iterator();
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

    public synchronized void stopStatisticsMonitor() {
        stopScheduledStatisticsMonitor();
        for (OperatingSystem operatingSystem : operatingSystemsByUID.values()) {
            operatingSystem.stopStatisticsMonitor();
        }
    }

    private void stopScheduledStatisticsMonitor() {
        if (scheduledStatisticsRefCount!=0 && --scheduledStatisticsRefCount > 0) return;
        
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
            scheduledStatisticsMonitor = null;
        }
    }

    public synchronized boolean isMonitoring() {
        return scheduledStatisticsMonitor != null;
    }

    private void rescheduleStatisticsMonitor() {
        if (scheduledStatisticsRefCount++ > 0) return;
        
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
        }
        scheduledStatisticsMonitor = admin.scheduleWithFixedDelay(new Runnable() {
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

    public void addOperatingSystem(final OperatingSystem operatingSystem) {
        assertStateChangesPermitted();
        OperatingSystem existing = operatingSystemsByUID.put(operatingSystem.getUid(), operatingSystem);
        if (existing == null) {
            if (isMonitoring()) {
                admin.raiseEvent(this, new Runnable() {
                    public void run() {
                        operatingSystem.setStatisticsHistorySize(statisticsHistorySize);
                        operatingSystem.setStatisticsInterval(statisticsInterval, TimeUnit.MILLISECONDS);
                        operatingSystem.startStatisticsMonitor();
                    }
                });
            }
        }
    }

    public void removeOperatingSystem(String uid) {
        assertStateChangesPermitted();
        OperatingSystem existing = operatingSystemsByUID.remove(uid);
        if (existing != null) {
            existing.stopStatisticsMonitor();
        }
    }
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }
}
