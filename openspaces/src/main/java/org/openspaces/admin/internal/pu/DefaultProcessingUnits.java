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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticAutoScalingFailureEventManager;
import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticAutoScalingProgressChangedEventManager;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticAutoScalingFailureEventManager;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticAutoScalingProgressChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultBackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitAddedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceProvisionStatusChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitRemovedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitStatusChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalBackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitAddedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceProvisionStatusChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitRemovedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitStatusChangedEventManager;
import org.openspaces.admin.internal.space.InternalSpace;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.elastic.events.ElasticAutoScalingFailureEvent;
import org.openspaces.admin.pu.elastic.events.ElasticAutoScalingFailureEventManager;
import org.openspaces.admin.pu.elastic.events.ElasticAutoScalingProgressChangedEvent;
import org.openspaces.admin.pu.elastic.events.ElasticAutoScalingProgressChangedEventManager;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;
import org.openspaces.admin.pu.elastic.events.ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventManager;

import com.j_spaces.kernel.SizeConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultProcessingUnits implements InternalProcessingUnits {

    private final InternalAdmin admin;

    private final Map<String, ProcessingUnit> processingUnits = new SizeConcurrentHashMap<String, ProcessingUnit>();

    private final InternalProcessingUnitAddedEventManager processingUnitAddedEventManager;

    private final InternalProcessingUnitRemovedEventManager processingUnitRemovedEventManager;

    private final InternalManagingGridServiceManagerChangedEventManager managingGridServiceManagerChangedEventManager;

    private final InternalBackupGridServiceManagerChangedEventManager backupGridServiceManagerChangedEventManager;

    private final InternalProcessingUnitStatusChangedEventManager processingUnitStatusChangedEventManager;

    private final InternalProcessingUnitInstanceAddedEventManager processingUnitInstanceAddedEventManager;

    private final InternalProcessingUnitInstanceRemovedEventManager processingUnitInstanceRemovedEventManager;

    private final InternalProcessingUnitInstanceStatisticsChangedEventManager processingUnitInstanceStatisticsChangedEventManager;

    private final InternalProcessingUnitInstanceProvisionStatusChangedEventManager processingUnitInstanceProvisionStatusChangedEventManager;
    private final InternalProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager processingUnitInstanceMemberAliveIndicatorStatusChangedEventManager;
    
    private final InternalElasticAutoScalingProgressChangedEventManager elasticAutoScalingProgressChangedEventManager;
    private final InternalElasticAutoScalingFailureEventManager elasticAutoScalingFailureEventManager;

    
    private volatile long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private volatile int statisticsHistorySize = StatisticsMonitor.DEFAULT_HISTORY_SIZE;

    private volatile boolean scheduledStatisticsMonitor = false;

	private Map<String, Integer> plannedNumberOfInstances = new ConcurrentHashMap<String, Integer>();

    public DefaultProcessingUnits(InternalAdmin admin) {
        this.admin = admin;
        this.processingUnitAddedEventManager = new DefaultProcessingUnitAddedEventManager(this);
        this.processingUnitRemovedEventManager = new DefaultProcessingUnitRemovedEventManager(this);

        this.managingGridServiceManagerChangedEventManager = new DefaultManagingGridServiceManagerChangedEventManager(admin);
        this.backupGridServiceManagerChangedEventManager = new DefaultBackupGridServiceManagerChangedEventManager(admin);
        this.processingUnitStatusChangedEventManager = new DefaultProcessingUnitStatusChangedEventManager(admin);

        this.processingUnitInstanceAddedEventManager = new DefaultProcessingUnitInstanceAddedEventManager(this, admin);
        this.processingUnitInstanceRemovedEventManager = new DefaultProcessingUnitInstanceRemovedEventManager(admin);

        this.processingUnitInstanceStatisticsChangedEventManager = new DefaultProcessingUnitInstanceStatisticsChangedEventManager(admin);
        
        this.processingUnitInstanceProvisionStatusChangedEventManager = new DefaultProcessingUnitInstanceProvisionStatusChangedEventManager(admin);
        this.processingUnitInstanceMemberAliveIndicatorStatusChangedEventManager = new DefaultProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager(admin);
        
        this.elasticAutoScalingProgressChangedEventManager = new DefaultElasticAutoScalingProgressChangedEventManager(admin);
        this.elasticAutoScalingFailureEventManager = new DefaultElasticAutoScalingFailureEventManager(admin);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public ProcessingUnitAddedEventManager getProcessingUnitAdded() {
        return this.processingUnitAddedEventManager;
    }

    public ProcessingUnitRemovedEventManager getProcessingUnitRemoved() {
        return this.processingUnitRemovedEventManager;
    }

    public ManagingGridServiceManagerChangedEventManager getManagingGridServiceManagerChanged() {
        return this.managingGridServiceManagerChangedEventManager;
    }

    public BackupGridServiceManagerChangedEventManager getBackupGridServiceManagerChanged() {
        return this.backupGridServiceManagerChangedEventManager;
    }

    public ProcessingUnitStatusChangedEventManager getProcessingUnitStatusChanged() {
        return this.processingUnitStatusChangedEventManager;
    }

    public ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded() {
        return this.processingUnitInstanceAddedEventManager;
    }

    public ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved() {
        return this.processingUnitInstanceRemovedEventManager;
    }

    public Iterator<ProcessingUnit> iterator() {
        return Collections.unmodifiableCollection(processingUnits.values()).iterator();
    }

    public ProcessingUnit[] getProcessingUnits() {
        return processingUnits.values().toArray(new ProcessingUnit[0]);
    }

    public ProcessingUnitInstance[] getProcessingUnitInstances() {
        List<ProcessingUnitInstance> processingUnitInstances = new ArrayList<ProcessingUnitInstance>();
        for (ProcessingUnit processingUnit : this) {
            for (ProcessingUnitInstance processingUnitInstance : processingUnit) {
                processingUnitInstances.add(processingUnitInstance);
            }
        }
        return processingUnitInstances.toArray(new ProcessingUnitInstance[processingUnitInstances.size()]);
    }

    public ProcessingUnit getProcessingUnit(String name) {
        return processingUnits.get(name);
    }

    public Map<String, ProcessingUnit> getNames() {
        return Collections.unmodifiableMap(processingUnits);
    }

    public int getSize() {
        return processingUnits.size();
    }

    public boolean isEmpty() {
        return processingUnits.isEmpty();
    }

    public ProcessingUnit waitFor(String processingUnitName) {
        return waitFor(processingUnitName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnit waitFor(final String processingUnitName, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<ProcessingUnit> ref = new AtomicReference<ProcessingUnit>();
        ProcessingUnitAddedEventListener added = new ProcessingUnitAddedEventListener() {
            public void processingUnitAdded(ProcessingUnit processingUnit) {
                if (processingUnitName.equals(processingUnit.getName())) {
                    ref.set(processingUnit);
                    latch.countDown();
                }
            }
        };
        getProcessingUnitAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getProcessingUnitAdded().remove(added);
        }
    }

    public void addLifecycleListener(ProcessingUnitLifecycleEventListener eventListener) {
        getProcessingUnitAdded().add(eventListener);
        getProcessingUnitRemoved().add(eventListener);
        getProcessingUnitStatusChanged().add(eventListener);
        getManagingGridServiceManagerChanged().add(eventListener);
        getBackupGridServiceManagerChanged().add(eventListener);
    }

    public void removeLifecycleListener(ProcessingUnitLifecycleEventListener eventListener) {
        getProcessingUnitAdded().remove(eventListener);
        getProcessingUnitRemoved().remove(eventListener);
        getProcessingUnitStatusChanged().remove(eventListener);
        getManagingGridServiceManagerChanged().remove(eventListener);
        getBackupGridServiceManagerChanged().remove(eventListener);
    }

    public void addLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        getProcessingUnitInstanceAdded().add(eventListener);
        getProcessingUnitInstanceRemoved().add(eventListener);
    }

    public void removeLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        getProcessingUnitInstanceAdded().remove(eventListener);
        getProcessingUnitInstanceRemoved().remove(eventListener);
    }

    public void addProcessingUnit(final ProcessingUnit processingUnit) {
        assertStateChangesPermitted();
        ProcessingUnit existingProcessingUnit = processingUnits.put(processingUnit.getName(), processingUnit);
        if (existingProcessingUnit == null) {
            processingUnitAddedEventManager.processingUnitAdded(processingUnit);
        }
        processingUnit.setStatisticsInterval(statisticsInterval, TimeUnit.MILLISECONDS);
        processingUnit.setStatisticsHistorySize(statisticsHistorySize);
        if (isMonitoring()) {
            admin.raiseEvent(this, new Runnable() {
                public void run() {
                    processingUnit.startStatisticsMonitor();
                }
            });
        }
    }

    public void removeProcessingUnit(String name) {
        assertStateChangesPermitted();
        final ProcessingUnit existingProcessingUnit = processingUnits.remove(name);
        if (existingProcessingUnit != null) {
            existingProcessingUnit.stopStatisticsMonitor();
            ((InternalProcessingUnit) existingProcessingUnit).setStatus(0);
            processingUnitRemovedEventManager.processingUnitRemoved(existingProcessingUnit);
        }
    }

    public ProcessingUnitInstanceStatisticsChangedEventManager getProcessingUnitInstanceStatisticsChanged() {
        return this.processingUnitInstanceStatisticsChangedEventManager;
    }
    
    @Override
    public ProcessingUnitInstanceProvisionStatusChangedEventManager getProcessingUnitInstanceProvisionStatusChanged() {
        return processingUnitInstanceProvisionStatusChangedEventManager;
    }
    
    @Override
    public ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager getProcessingUnitInstanceMemberAliveIndicatorStatusChanged() {
        return processingUnitInstanceMemberAliveIndicatorStatusChangedEventManager;
    }

    public void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        statisticsInterval = timeUnit.toMillis(interval);
        for (ProcessingUnit processingUnit : processingUnits.values()) {
            processingUnit.setStatisticsInterval(statisticsInterval, TimeUnit.MILLISECONDS);
        }
    }

    public void setStatisticsHistorySize(int historySize) {
        this.statisticsHistorySize = historySize;
        for (ProcessingUnit processingUnit : processingUnits.values()) {
            processingUnit.setStatisticsHistorySize(statisticsHistorySize);
        }
    }

    public void startStatisticsMonitor() {
        scheduledStatisticsMonitor = true;
        for (ProcessingUnit processingUnit : processingUnits.values()) {
            processingUnit.startStatisticsMonitor();
        }
    }

    public void stopStatisticsMonitor() {
        scheduledStatisticsMonitor = false;
        for (ProcessingUnit processingUnit : processingUnits.values()) {
            processingUnit.stopStatisticsMonitor();
        }
    }

    public boolean isMonitoring() {
        return scheduledStatisticsMonitor;
    }
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }

    @Override
    public ElasticAutoScalingProgressChangedEventManager getElasticAutoScalingProgressChanged() {
        return elasticAutoScalingProgressChangedEventManager;
    }

    @Override
    public ElasticAutoScalingFailureEventManager getElasticAutoScalingFailure() {
        return elasticAutoScalingFailureEventManager;
    }
    
    @Override
    public void processElasticScaleStrategyEvent(final ElasticProcessingUnitEvent event) {

    	if ( event instanceof ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent ) {
            ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent puEvent = ( ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent ) event;
            if ( puEvent.getGridServiceAgentZones() == null && puEvent.getProcessingUnitName() != null)
            {
                // total # of instances - without per-zone plan
                final int newPlan = puEvent.getNewPlannedNumberOfInstances();
                assertStateChangesPermitted();
                plannedNumberOfInstances.put( event.getProcessingUnitName(), newPlan );
            }
        }

        if (event instanceof ElasticAutoScalingFailureEvent) {
            elasticAutoScalingFailureEventManager.elasticAutoScalingFailure((ElasticAutoScalingFailureEvent)event);
        }
        else if (event instanceof ElasticAutoScalingProgressChangedEvent) {
            elasticAutoScalingProgressChangedEventManager.elasticAutoScalingProgressChanged((ElasticAutoScalingProgressChangedEvent)event);
        }
    }

    @Override
    public ProcessingUnit removeEmbeddedSpace(InternalSpace space) {
        assertStateChangesPermitted();
        for (ProcessingUnit pu : this.processingUnits.values()) {
            if (((InternalProcessingUnit) pu).removeEmbeddedSpace(space)) {
                return pu;
            }
        }
        return null;
    }
    
    /**
     * @return The planned number of instances for the specified pu name, regardless of the GSM and the LUS status of this pu. Or null if not managed by ESM.
     */
    @Override
	public Integer getPlannedNumberOfInstances(final ProcessingUnit pu) {
		return plannedNumberOfInstances.get(pu.getName());
	}

}
