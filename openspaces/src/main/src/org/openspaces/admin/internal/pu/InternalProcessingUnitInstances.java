package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;

import java.util.Iterator;

/**
 * @author kimchy
 */
public interface InternalProcessingUnitInstances extends InternalProcessingUnitInstancesAware {

    void addOrphaned(ProcessingUnitInstance processingUnitInstance);

    ProcessingUnitInstance removeOrphaned(String uid);

    void addInstance(ProcessingUnitInstance processingUnitInstance);

    ProcessingUnitInstance removeInstance(String uid);

    ProcessingUnitInstance[] getOrphaned();

    Iterator<ProcessingUnitInstance> getInstancesIt();

    ProcessingUnitInstance[] getInstances();

    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();

    void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);
}
