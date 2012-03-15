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

import com.gigaspaces.internal.os.OSDetails;

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

    private int statisticsHistorySize = StatisticsMonitor.DEFAULT_HISTORY_SIZE;

    private long lastStatisticsTimestamp = 0;

    private OperatingSystemStatistics lastStatistics;

    private Future scheduledStatisticsMonitor;
    private int scheduledStatisticsRefCount = 0;

    private volatile long timeDelta = Integer.MIN_VALUE;

    public DefaultOperatingSystem(OSDetails osDetails, InternalOperatingSystems operatingSystems) {
        this.details = new DefaultOperatingSystemDetails(osDetails);
        this.uid = details.getUid();
        this.operatingSystems = operatingSystems;
        if (operatingSystems != null) {
            this.admin = (InternalAdmin) operatingSystems.getAdmin();
        } else {
            this.admin = null;
        }

        this.statisticsChangedEventManager = new DefaultOperatingSystemStatisticsChangedEventManager(admin, this);
        if (admin != null) {
            // compute the time delta on a thread pool
            admin.getScheduler().schedule(new Runnable() {
                public void run() {
                    long localTimestamp = System.currentTimeMillis();
                    long machineCurrentTimestamp = getCurrentTimeInMillis();
                    if ((System.currentTimeMillis() - localTimestamp) > 200) {
                        // if it took more than 200 millisecond to get the timestamp, something is slow with the network
                        // try and fetch the delta again
                        admin.getScheduler().schedule(this, 50, TimeUnit.MILLISECONDS);
                    } else {
                        if (machineCurrentTimestamp != -1) {
                            timeDelta = localTimestamp - machineCurrentTimestamp;
                        } else {
                            admin.getScheduler().schedule(this, 50, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            }, 10, TimeUnit.MILLISECONDS);
        }
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public void addOperatingSystemInfoProvider(InternalOperatingSystemInfoProvider provider) {
        
        assertStateChangesPermitted();
        operatingSystemInfoProviders.add(provider);
    }

    public void removeOperatingSystemInfoProvider(InternalOperatingSystemInfoProvider provider) {
        assertStateChangesPermitted();
        operatingSystemInfoProviders.remove(provider);
    }

    public boolean hasOperatingSystemInfoProviders() {
        return !operatingSystemInfoProviders.isEmpty();
    }

    public String getUid() {
        return this.uid;
    }

    public long getTimeDelta() {
        return timeDelta;
    }

    public long getCurrentTimeInMillis() {
        for (InternalOperatingSystemInfoProvider provider : operatingSystemInfoProviders) {
            try {
                return provider.getCurrentTimeInMillis();
            } catch (RemoteException e) {
                // simply try the next one
            }
        }
        return -1;
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
                lastStatistics = new DefaultOperatingSystemStatistics(provider.getOSStatistics(), getDetails(), previousStats, statisticsHistorySize, timeDelta);
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
        final OperatingSystem operatingSystem = this;
        scheduledStatisticsMonitor = admin.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                OperatingSystemStatistics stats = operatingSystem.getStatistics();
                OperatingSystemStatisticsChangedEvent event = new OperatingSystemStatisticsChangedEvent(operatingSystem, stats);
                statisticsChangedEventManager.operatingSystemStatisticsChanged(event);
                ((InternalOperatingSystemStatisticsChangedEventManager) operatingSystems.getOperatingSystemStatisticsChanged()).operatingSystemStatisticsChanged(event);
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
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }
}
