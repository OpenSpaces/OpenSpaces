package org.openspaces.admin.internal.pu;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitInstances implements InternalProcessingUnitInstances {

    private final InternalAdmin admin;

    private final Map<String, ProcessingUnitInstance> orphanedProcessingUnitInstances = new ConcurrentHashMap<String, ProcessingUnitInstance>();

    private final Map<String, ProcessingUnitInstance> processingUnitInstances = new ConcurrentHashMap<String, ProcessingUnitInstance>();

    private final InternalProcessingUnitInstanceAddedEventManager processingUnitInstanceAddedEventManager;

    private final InternalProcessingUnitInstanceRemovedEventManager processingUnitInstanceRemovedEventManager;

    public DefaultProcessingUnitInstances(InternalAdmin admin) {
        this.admin = admin;
        this.processingUnitInstanceAddedEventManager = new DefaultProcessingUnitInstanceAddedEventManager(this, admin);
        this.processingUnitInstanceRemovedEventManager = new DefaultProcessingUnitInstanceRemovedEventManager(admin);
    }

    public void addOrphaned(ProcessingUnitInstance processingUnitInstance) {
        orphanedProcessingUnitInstances.put(processingUnitInstance.getUid(), processingUnitInstance);
    }

    public ProcessingUnitInstance removeOrphaned(String uid) {
        return orphanedProcessingUnitInstances.remove(uid);
    }

    public void addInstance(ProcessingUnitInstance processingUnitInstance) {
        ProcessingUnitInstance existingPU = processingUnitInstances.put(processingUnitInstance.getUid(), processingUnitInstance);
        if (existingPU == null) {
            processingUnitInstanceAddedEventManager.processingUnitInstanceAdded(processingUnitInstance);
        }
    }

    public ProcessingUnitInstance removeInstance(String uid) {
        ProcessingUnitInstance processingUnitInstance = processingUnitInstances.remove(uid);
        if (processingUnitInstance != null) {
            processingUnitInstanceRemovedEventManager.processingUnitInstanceRemoved(processingUnitInstance);
        }
        return processingUnitInstance;
    }

    public ProcessingUnitInstance[] getOrphaned() {
        return orphanedProcessingUnitInstances.values().toArray(new ProcessingUnitInstance[0]);
    }

    public Iterator<ProcessingUnitInstance> getInstancesIt() {
        return processingUnitInstances.values().iterator();
    }

    public ProcessingUnitInstance[] getProcessingUnitInstances() {
        return getInstances();
    }

    public ProcessingUnitInstance[] getInstances() {
        return processingUnitInstances.values().toArray(new ProcessingUnitInstance[0]);
    }

    public ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded() {
        return this.processingUnitInstanceAddedEventManager;
    }

    public ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved() {
        return this.processingUnitInstanceRemovedEventManager;
    }

    public void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstanceAddedEventManager.add(eventListener);
        processingUnitInstanceRemovedEventManager.add(eventListener);
    }

    public void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstanceAddedEventManager.remove(eventListener);
        processingUnitInstanceRemovedEventManager.remove(eventListener);
    }
}
