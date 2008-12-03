package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.GridServiceContainerLifecycleEventListener;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.events.GridServiceManagerLifecycleEventListener;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.events.LookupServiceLifecycleEventListener;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineLifecycleEventListener;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEvent;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.events.SpaceLifecycleEventListener;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.events.VirtualMachineLifecycleEventListener;

/**
 * @author kimchy
 */
public class TestEventSampler implements MachineLifecycleEventListener,
        GridServiceContainerLifecycleEventListener,
        GridServiceManagerLifecycleEventListener,
        ProcessingUnitLifecycleEventListener,
        ProcessingUnitInstanceLifecycleEventListener,
        LookupServiceLifecycleEventListener,
        VirtualMachineLifecycleEventListener,
        SpaceLifecycleEventListener {

    public static void main(String[] args) throws Exception {
        TestEventSampler eventSampler = new TestEventSampler();
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        admin.addEventListener(eventSampler);

        Thread.sleep(10000000);
    }

    public void machineAdded(Machine machine) {
        System.out.println("Machine Added [" + machine.getUid() + "]");
    }

    public void machineRemoved(Machine machine) {
        System.out.println("Machine Removed [" + machine.getUid() + "]");
    }

    public void processingUnitAdded(ProcessingUnit processingUnit) {
        System.out.println("Processing Unit Added [" + processingUnit.getName() + "]");
    }

    public void processingUnitRemoved(ProcessingUnit processingUnit) {
        System.out.println("Processing Unit Removed [" + processingUnit.getName() + "]");
    }

    public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
        System.out.println("Processing Unit Instance Added [" + processingUnitInstance.getClusterInfo() + "]");
    }

    public void processingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance) {
        System.out.println("Processing Unit Instance Removed [" + processingUnitInstance.getClusterInfo() + "]");
    }

    public void gridServiceManagerAdded(GridServiceManager gridServiceManager) {
        System.out.println("GSM Added [" + gridServiceManager.getUid() + "]");
    }

    public void gridServiceManagerRemoved(GridServiceManager gridServiceManager) {
        System.out.println("GSM Removed [" + gridServiceManager.getUid() + "]");
    }

    public void gridServiceContainerAdded(GridServiceContainer gridServiceContainer) {
        System.out.println("GSC Added [" + gridServiceContainer.getUid() + "]");
    }

    public void gridServiceContainerRemoved(GridServiceContainer gridServiceContainer) {
        System.out.println("GSC Removed [" + gridServiceContainer.getUid() + "]");
    }

    public void lookupServiceAdded(LookupService lookupService) {
        System.out.println("LUS Added [" + lookupService.getUid() + "]");
    }

    public void lookupServiceRemoved(LookupService lookupService) {
        System.out.println("LUS Removed [" + lookupService.getUid() + "]");
    }

    public void virtualMachineAdded(VirtualMachine virtualMachine) {
        System.out.println("VM Added [" + virtualMachine.getUid() + "]");
    }

    public void virtualMachineRemoved(VirtualMachine virtualMachine) {
        System.out.println("VM Removed [" + virtualMachine.getUid() + "]");
    }

    public void processingUnitStatusChanged(ProcessingUnitStatusChangedEvent event) {
        System.out.println("PU [" + event.getProcessingUnit().getName() + "] Status changed from [" + event.getPreviousStatus() + "] to [" + event.getNewStatus() + "]");
    }

    public void processingUnitManagingGridServiceManagerChanged(ManagingGridServiceManagerChangedEvent event) {
        if (event.isUnknown()) {
            System.out.println("Processing Unit [" + event.getProcessingUnit().getName() + "] managin GSM UNKNOWN");
        } else {
            System.out.println("Processing Unit [" + event.getProcessingUnit().getName() + "] new managing GSM [" + event.getNewGridServiceManager().getUid() + "]");
        }
    }

    public void processingUnitBackupGridServiceManagerChanged(BackupGridServiceManagerChangedEvent event) {
        System.out.println("Processing Unit [" + event.getProcessingUnit().getName() + "] Backup GSM [" + event.getType() + "] with uid [" + event.getGridServiceManager().getUid() + "]");
    }

    public void spaceAdded(Space space) {
        System.out.println("Space Added [" + space.getUid() + "]");
    }

    public void spaceRemoved(Space space) {
        System.out.println("Space Removed [" + space.getUid() + "]");
    }
}
