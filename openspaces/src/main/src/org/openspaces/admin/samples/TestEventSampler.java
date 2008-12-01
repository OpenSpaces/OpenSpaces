package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainerEventListener;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.GridServiceManagerEventListener;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.events.LookupServiceAddedEventListener;
import org.openspaces.admin.lus.events.LookupServiceRemovedEventListener;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineAddedEventListener;
import org.openspaces.admin.machine.events.MachineRemovedEventListener;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitEventListener;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachineEventListener;

/**
 * @author kimchy
 */
public class TestEventSampler implements MachineAddedEventListener, MachineRemovedEventListener, ProcessingUnitEventListener,
        GridServiceManagerEventListener, GridServiceContainerEventListener,
        LookupServiceAddedEventListener, LookupServiceRemovedEventListener,
        VirtualMachineEventListener {

    public static void main(String[] args) throws Exception {
        TestEventSampler eventSampler = new TestEventSampler();
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        admin.getMachines().getMachineAdded().add(eventSampler);
        admin.getMachines().getMachineRemoved().add(eventSampler);
        admin.getProcessingUnits().addEventListener(eventSampler);
        admin.getLookupServices().getLookupServiceAdded().add(eventSampler);
        admin.getLookupServices().getLookupServiceRemoved().add(eventSampler);
        admin.getGridServiceManagers().addEventListener(eventSampler);
        admin.getGridServiceContainers().addEventListener(eventSampler);
        admin.getVirtualMachines().addEventListener(eventSampler);

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

    public void processingUnitStatusChanged(ProcessingUnit processingUnit, DeploymentStatus oldStatus, DeploymentStatus newStatus) {
        System.out.println("Processing Unit Deployment Status changed from [" + oldStatus + "] to [" + newStatus +"]");
    }

    public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
        System.out.println("Processing Unit Instance Added [" + processingUnitInstance.getClusterInfo() + "]");
    }

    public void processingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance) {
        System.out.println("Processing Unit Instance Removed [" + processingUnitInstance.getClusterInfo() + "]");
    }

    public void processingUnitManagingGridServiceManagerSet(ProcessingUnit processingUnit, GridServiceManager oldManaingGridServiceManager, GridServiceManager newManaingGridServiceManager) {
        System.out.println("Processing Unit [" + processingUnit.getName() + "] Managing GSM [" + newManaingGridServiceManager.getUid() + "]");
    }

    public void processingUnitManagingGridServiceManagerUnknown(ProcessingUnit processingUnit) {
        System.out.println("Processing Unit [" + processingUnit.getName() + "] Managing GSM UNKNOWN");
    }

    public void processingUnitBackupGridServiceManagerAdded(ProcessingUnit processingUnit, GridServiceManager gridServiceManager) {
        System.out.println("Processing Unit [" + processingUnit.getName() + "] Backup GSM Added [" + gridServiceManager.getUid() + "]");
    }

    public void processingUnitBackupGridServiceManagerRemoved(ProcessingUnit processingUnit, GridServiceManager gridServiceManager) {
        System.out.println("Processing Unit [" + processingUnit.getName() + "] Backup GSM Removed [" + gridServiceManager.getUid() + "]");
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
}
