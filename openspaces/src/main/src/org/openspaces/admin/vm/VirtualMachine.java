package org.openspaces.admin.vm;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.machine.MachineAware;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.space.SpaceInstance;

/**
 * @author kimchy
 */
public interface VirtualMachine extends MachineAware {

    String getUid();

    VirtualMachineDetails getDetails();

    VirtualMachineStatistics getStatistics();

    /**
     * Returns the grid service manager started within this virtual machine.
     * Returns <code>null</code> if no grid service manager was started within it.
     */
    GridServiceManager getGridServiceManager();

    /**
     * Returns the grid service container started within this virtual machine.
     * Returns <code>null</code> if no grid service manager was started within it.
     */
    GridServiceContainer getGridServiceContainer();

    /**
     * Returns the processing unit instnaces started within this virtual machine.
     */
    ProcessingUnitInstance[] getProcessingUnitInstances();

    /**
     * Returns the space instnaces started within this virtual machine.
     */
    SpaceInstance[] getSpaceInstances();

    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();

    void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);
}
