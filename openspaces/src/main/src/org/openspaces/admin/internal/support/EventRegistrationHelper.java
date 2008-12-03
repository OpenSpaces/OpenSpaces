package org.openspaces.admin.internal.support;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.lus.events.LookupServiceAddedEventListener;
import org.openspaces.admin.lus.events.LookupServiceRemovedEventListener;
import org.openspaces.admin.machine.events.MachineAddedEventListener;
import org.openspaces.admin.machine.events.MachineRemovedEventListener;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventListener;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventListener;
import org.openspaces.admin.vm.events.VirtualMachineAddedEventListener;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventListener;

/**
 * @author kimchy
 */
public abstract class EventRegistrationHelper {

    public static void addEventListener(InternalAdmin admin, AdminEventListener eventListener) {
        if (eventListener instanceof MachineAddedEventListener) {
            admin.getMachines().getMachineAdded().add((MachineAddedEventListener) eventListener);
        }
        if (eventListener instanceof MachineRemovedEventListener) {
            admin.getMachines().getMachineRemoved().add((MachineRemovedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineAddedEventListener) {
            admin.getVirtualMachines().getVirtualMachineAdded().add((VirtualMachineAddedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineRemovedEventListener) {
            admin.getVirtualMachines().getVirtualMachineRemoved().add((VirtualMachineRemovedEventListener) eventListener);
        }
        if (eventListener instanceof LookupServiceAddedEventListener) {
            admin.getLookupServices().getLookupServiceAdded().add((LookupServiceAddedEventListener) eventListener);
        }
        if (eventListener instanceof LookupServiceRemovedEventListener) {
            admin.getLookupServices().getLookupServiceRemoved().add((LookupServiceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceManagerAddedEventListener) {
            admin.getGridServiceManagers().getGridServiceManagerAdded().add((GridServiceManagerAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceManagerRemovedEventListener) {
            admin.getGridServiceManagers().getGridServiceManagerRemoved().add((GridServiceManagerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceContainerAddedEventListener) {
            admin.getGridServiceContainers().getGridServiceContainerAdded().add((GridServiceContainerAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceContainerRemovedEventListener) {
            admin.getGridServiceContainers().getGridServiceContainerRemoved().add((GridServiceContainerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitAddedEventListener) {
            admin.getProcessingUnits().getProcessingUnitAdded().add((ProcessingUnitAddedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitRemovedEventListener) {
            admin.getProcessingUnits().getProcessingUnitRemoved().add((ProcessingUnitRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitStatusChangedEventListener) {
            admin.getProcessingUnits().getProcessingUnitStatusChanged().add((ProcessingUnitStatusChangedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceAddedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().add((ProcessingUnitInstanceAddedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceRemovedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceRemoved().add((ProcessingUnitInstanceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ManagingGridServiceManagerChangedEventListener) {
            admin.getProcessingUnits().getManagingGridServiceManagerChanged().add((ManagingGridServiceManagerChangedEventListener) eventListener);
        }
        if (eventListener instanceof BackupGridServiceManagerChangedEventListener) {
            admin.getProcessingUnits().getBackupGridServiceManagerChanged().add((BackupGridServiceManagerChangedEventListener) eventListener);
        }
    }

    public static void removeEventListener(InternalAdmin admin, AdminEventListener eventListener) {
        if (eventListener instanceof MachineAddedEventListener) {
            admin.getMachines().getMachineAdded().remove((MachineAddedEventListener) eventListener);
        }
        if (eventListener instanceof MachineRemovedEventListener) {
            admin.getMachines().getMachineRemoved().remove((MachineRemovedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineAddedEventListener) {
            admin.getVirtualMachines().getVirtualMachineAdded().remove((VirtualMachineAddedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineRemovedEventListener) {
            admin.getVirtualMachines().getVirtualMachineRemoved().remove((VirtualMachineRemovedEventListener) eventListener);
        }
        if (eventListener instanceof LookupServiceAddedEventListener) {
            admin.getLookupServices().getLookupServiceAdded().remove((LookupServiceAddedEventListener) eventListener);
        }
        if (eventListener instanceof LookupServiceRemovedEventListener) {
            admin.getLookupServices().getLookupServiceRemoved().remove((LookupServiceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceManagerAddedEventListener) {
            admin.getGridServiceManagers().getGridServiceManagerAdded().remove((GridServiceManagerAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceManagerRemovedEventListener) {
            admin.getGridServiceManagers().getGridServiceManagerRemoved().remove((GridServiceManagerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceContainerAddedEventListener) {
            admin.getGridServiceContainers().getGridServiceContainerAdded().remove((GridServiceContainerAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceContainerRemovedEventListener) {
            admin.getGridServiceContainers().getGridServiceContainerRemoved().remove((GridServiceContainerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitAddedEventListener) {
            admin.getProcessingUnits().getProcessingUnitAdded().remove((ProcessingUnitAddedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitRemovedEventListener) {
            admin.getProcessingUnits().getProcessingUnitRemoved().remove((ProcessingUnitRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitStatusChangedEventListener) {
            admin.getProcessingUnits().getProcessingUnitStatusChanged().remove((ProcessingUnitStatusChangedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceAddedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove((ProcessingUnitInstanceAddedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceRemovedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceRemoved().remove((ProcessingUnitInstanceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ManagingGridServiceManagerChangedEventListener) {
            admin.getProcessingUnits().getManagingGridServiceManagerChanged().remove((ManagingGridServiceManagerChangedEventListener) eventListener);
        }
        if (eventListener instanceof BackupGridServiceManagerChangedEventListener) {
            admin.getProcessingUnits().getBackupGridServiceManagerChanged().remove((BackupGridServiceManagerChangedEventListener) eventListener);
        }
    }
}
