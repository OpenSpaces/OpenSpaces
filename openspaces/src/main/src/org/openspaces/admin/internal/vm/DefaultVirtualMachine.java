package org.openspaces.admin.internal.vm;

import com.gigaspaces.jvm.JVMDetails;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsc.DefaultGridServiceContainers;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainers;
import org.openspaces.admin.internal.gsm.DefaultGridServiceManagers;
import org.openspaces.admin.internal.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.pu.DefaultProcessingUnitInstances;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstances;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.vm.VirtualMachineDetails;
import org.openspaces.admin.vm.VirtualMachineStatistics;
import org.openspaces.core.util.ConcurrentHashSet;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultVirtualMachine implements InternalVirtualMachine {

    private final InternalAdmin admin;

    private final String uid;

    private final VirtualMachineDetails details;

    private final Set<InternalVirtualMachineInfoProvider> virtualMachineInfoProviders = new ConcurrentHashSet<InternalVirtualMachineInfoProvider>();

    private volatile Machine machine;

    private final InternalGridServiceManagers gridServiceManagers;

    private final InternalGridServiceContainers gridServiceContainers;

    private final InternalProcessingUnitInstances processingUnitInstances;

    private final Map<String, SpaceInstance> spaceInstances = new ConcurrentHashMap<String, SpaceInstance>();

    public DefaultVirtualMachine(InternalAdmin admin, JVMDetails details) {
        this.admin = admin;
        this.details = new DefaultVirtualMachineDetails(details);
        this.uid = details.getUid();
        this.gridServiceManagers = new DefaultGridServiceManagers(admin);
        this.gridServiceContainers = new DefaultGridServiceContainers(admin);
        this.processingUnitInstances = new DefaultProcessingUnitInstances(admin);
    }

    public String getUid() {
        return this.uid;
    }

    public void addVirtualMachineInfoProvider(InternalVirtualMachineInfoProvider virtualMachineInfoProvider) {
        virtualMachineInfoProviders.add(virtualMachineInfoProvider);
    }

    public void removeVirtualMachineInfoProvider(InternalVirtualMachineInfoProvider virtualMachineInfoProvider) {
        virtualMachineInfoProviders.remove(virtualMachineInfoProvider);
    }

    public boolean hasVirtualMachineInfoProviders() {
        return !virtualMachineInfoProviders.isEmpty();
    }

    public VirtualMachineDetails getDetails() {
        return this.details;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public GridServiceManager getGridServiceManager() {
        Iterator<GridServiceManager> it = gridServiceManagers.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public GridServiceContainer getGridServiceContainer() {
        Iterator<GridServiceContainer> it = gridServiceContainers.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public GridServiceManagers getGridServiceManagers() {
        return gridServiceManagers;
    }

    public GridServiceContainers getGridServiceContainers() {
        return gridServiceContainers;
    }

    public ProcessingUnitInstance[] getProcessingUnitInstances() {
        return processingUnitInstances.getInstances();
    }

    public ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded() {
        return processingUnitInstances.getProcessingUnitInstanceAdded();
    }

    public ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved() {
        return processingUnitInstances.getProcessingUnitInstanceRemoved();
    }

    public void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstances.addProcessingUnitInstanceLifecycleEventListener(eventListener);
    }

    public void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstances.removeProcessingUnitInstanceLifecycleEventListener(eventListener);
    }

    public SpaceInstance[] getSpaceInstances() {
        return spaceInstances.values().toArray(new SpaceInstance[0]);
    }

    public void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance) {
        processingUnitInstances.addInstance(processingUnitInstance);
    }

    public void removeProcessingUnitInstance(String uid) {
        processingUnitInstances.removeInstnace(uid);
    }

    public void addSpaceInstance(SpaceInstance spaceInstance) {
        spaceInstances.put(spaceInstance.getUid(), spaceInstance);
    }

    public void removeSpaceInstance(String uid) {
        spaceInstances.remove(uid);
    }

    private static final VirtualMachineStatistics NA_STATS = new DefaultVirtualMachineStatistics();

    public VirtualMachineStatistics getStatistics() {
        for (InternalVirtualMachineInfoProvider provider : virtualMachineInfoProviders) {
            try {
                return new DefaultVirtualMachineStatistics(provider.getJVMStatistics());
            } catch (RemoteException e) {
                // continue to the next one
            }
        }
        // all failed, return NA
        return NA_STATS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultVirtualMachine that = (DefaultVirtualMachine) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
}
