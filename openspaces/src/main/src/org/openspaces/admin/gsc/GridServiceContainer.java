package org.openspaces.admin.gsc;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;

import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface GridServiceContainer extends GridComponent, Iterable<ProcessingUnitInstance> {

    boolean waitFor(int numberOfProcessingUnitInstances);

    boolean waitFor(int numberOfProcessingUnitInstances, long timeout, TimeUnit timeUnit);

    ProcessingUnitInstance[] getProcessingUnitInsances();

    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();

    void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);
}