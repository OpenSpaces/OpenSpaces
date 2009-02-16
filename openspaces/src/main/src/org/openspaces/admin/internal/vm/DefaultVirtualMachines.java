package org.openspaces.admin.internal.vm;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.vm.events.DefaultVirtualMachineAddedEventManager;
import org.openspaces.admin.internal.vm.events.DefaultVirtualMachineRemovedEventManager;
import org.openspaces.admin.internal.vm.events.DefaultVirtualMachineStatisticsChangedEventManager;
import org.openspaces.admin.internal.vm.events.DefaultVirtualMachinesStatisticsChangedEventManager;
import org.openspaces.admin.internal.vm.events.InternalVirtualMachineAddedEventManager;
import org.openspaces.admin.internal.vm.events.InternalVirtualMachineRemovedEventManager;
import org.openspaces.admin.internal.vm.events.InternalVirtualMachineStatisticsChangedEventManager;
import org.openspaces.admin.internal.vm.events.InternalVirtualMachinesStatisticsChangedEventManager;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachineDetails;
import org.openspaces.admin.vm.VirtualMachineStatistics;
import org.openspaces.admin.vm.VirtualMachinesDetails;
import org.openspaces.admin.vm.VirtualMachinesStatistics;
import org.openspaces.admin.vm.events.VirtualMachineAddedEventManager;
import org.openspaces.admin.vm.events.VirtualMachineLifecycleEventListener;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventManager;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventManager;
import org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEvent;
import org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEventManager;

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
public class DefaultVirtualMachines implements InternalVirtualMachines {

    private final InternalAdmin admin;

    private final Map<String, VirtualMachine> virtualMachinesByUID = new SizeConcurrentHashMap<String, VirtualMachine>();

    private final InternalVirtualMachineAddedEventManager virtualMachineAddedEventManager;

    private final InternalVirtualMachineRemovedEventManager virtualMachineRemovedEventManager;

    private final InternalVirtualMachineStatisticsChangedEventManager virtualMachineStatisticsChangedEventManager;

    private final InternalVirtualMachinesStatisticsChangedEventManager virtualMachinesStatisticsChangedEventManager;

    private long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private long lastStatisticsTimestamp = 0;

    private VirtualMachinesStatistics lastStatistics;
    
    private Future scheduledStatisticsMonitor;

    public DefaultVirtualMachines(InternalAdmin admin) {
        this.admin = admin;
        this.virtualMachineAddedEventManager = new DefaultVirtualMachineAddedEventManager(this);
        this.virtualMachineRemovedEventManager = new DefaultVirtualMachineRemovedEventManager(this);
        this.virtualMachineStatisticsChangedEventManager = new DefaultVirtualMachineStatisticsChangedEventManager(admin);
        this.virtualMachinesStatisticsChangedEventManager = new DefaultVirtualMachinesStatisticsChangedEventManager(admin);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public VirtualMachineAddedEventManager getVirtualMachineAdded() {
        return this.virtualMachineAddedEventManager;
    }

    public VirtualMachineRemovedEventManager getVirtualMachineRemoved() {
        return this.virtualMachineRemovedEventManager;
    }

    public VirtualMachineStatisticsChangedEventManager getVirtualMachineStatisticsChanged() {
        return this.virtualMachineStatisticsChangedEventManager;
    }

    public VirtualMachinesStatisticsChangedEventManager getStatisticsChanged() {
        return this.virtualMachinesStatisticsChangedEventManager;
    }

    public VirtualMachine[] getVirtualMachines() {
        return virtualMachinesByUID.values().toArray(new VirtualMachine[0]);
    }

    public Iterator<VirtualMachine> iterator() {
        return virtualMachinesByUID.values().iterator();
    }

    public int getSize() {
        return virtualMachinesByUID.size();
    }

    public boolean isEmpty() {
        return virtualMachinesByUID.size() == 0;
    }

    public synchronized VirtualMachinesStatistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        lastStatisticsTimestamp = currentTime;
        List<VirtualMachineDetails> details = new ArrayList<VirtualMachineDetails>();
        List<VirtualMachineStatistics> stats = new ArrayList<VirtualMachineStatistics>();
        for (VirtualMachine virtualMachine : virtualMachinesByUID.values()) {
            stats.add(virtualMachine.getStatistics());
            details.add(virtualMachine.getDetails());
        }
        lastStatistics = new DefaultVirtualMachinesStatistics(stats.toArray(new VirtualMachineStatistics[stats.size()]),
                new DefaultVirtualMachinesDetails(details.toArray(new VirtualMachineDetails[details.size()])),
                lastStatistics);
        return lastStatistics;
    }

    public VirtualMachinesDetails getDetails() {
        List<VirtualMachineDetails> details = new ArrayList<VirtualMachineDetails>();
        for (VirtualMachine virtualMachine : virtualMachinesByUID.values()) {
            details.add(virtualMachine.getDetails());
        }
        return new DefaultVirtualMachinesDetails(details.toArray(new VirtualMachineDetails[details.size()]));
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        statisticsInterval = timeUnit.toMillis(interval);
        if (isMonitoring()) {
            rescheduleStatisticsMonitor();
        }
        for (VirtualMachine virualMachine : virtualMachinesByUID.values()) {
            virualMachine.setStatisticsInterval(interval, timeUnit);
        }
    }

    public synchronized void startStatisticsMonitor() {
        rescheduleStatisticsMonitor();
        for (VirtualMachine virtualMachine : virtualMachinesByUID.values()) {
            virtualMachine.startStatisticsMonitor();
        }
    }

    public synchronized void stopStatisticsMontior() {
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
            scheduledStatisticsMonitor = null;
        }
        for (VirtualMachine virtualMachine : virtualMachinesByUID.values()) {
            virtualMachine.stopStatisticsMontior();
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
                VirtualMachinesStatistics stats = getStatistics();
                VirtualMachinesStatisticsChangedEvent event = new VirtualMachinesStatisticsChangedEvent(DefaultVirtualMachines.this, stats);
                virtualMachinesStatisticsChangedEventManager.virtualMachinesStatisticsChanged(event);
            }
        }, 0, statisticsInterval, TimeUnit.MILLISECONDS);
    }

    public void addLifecycleListener(VirtualMachineLifecycleEventListener eventListener) {
        getVirtualMachineAdded().add(eventListener);
        getVirtualMachineRemoved().add(eventListener);
    }

    public void removeLifecycleListener(VirtualMachineLifecycleEventListener eventListener) {
        getVirtualMachineAdded().remove(eventListener);
        getVirtualMachineRemoved().remove(eventListener);
    }

    public VirtualMachine getVirtualMachineByUID(String uid) {
        return virtualMachinesByUID.get(uid);
    }

    public Map<String, VirtualMachine> getUids() {
        return Collections.unmodifiableMap(virtualMachinesByUID);
    }

    public void addVirtualMachine(final VirtualMachine virtualMachine) {
        VirtualMachine existingVM = virtualMachinesByUID.put(virtualMachine.getUid(), virtualMachine);
        if (existingVM == null) {
            virtualMachine.setStatisticsInterval(statisticsInterval, TimeUnit.MILLISECONDS);
            if (isMonitoring()) {
                virtualMachine.startStatisticsMonitor();
            }
            virtualMachineAddedEventManager.virtualMachineAdded(virtualMachine);
        }
    }

    public InternalVirtualMachine removeVirtualMachine(String uid) {
        final InternalVirtualMachine existingVM = (InternalVirtualMachine) virtualMachinesByUID.remove(uid);
        if (existingVM != null) {
            existingVM.stopStatisticsMontior();
            virtualMachineRemovedEventManager.virtualMachineRemoved(existingVM);
        }
        return existingVM;
    }
}
