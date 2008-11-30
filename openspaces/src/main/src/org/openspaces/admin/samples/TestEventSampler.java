package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainerEventListener;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.GridServiceManagerEventListener;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.MachineEventListener;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitEventListener;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @author kimchy
 */
public class TestEventSampler implements MachineEventListener, ProcessingUnitEventListener,
        GridServiceManagerEventListener, GridServiceContainerEventListener {

    public static void main(String[] args) throws InterruptedException {
        TestEventSampler eventSampler = new TestEventSampler();
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        admin.getMachines().addEventListener(eventSampler);
        admin.getProcessingUnits().addEventListener(eventSampler);
        admin.getGridServiceManagers().addEventListener(eventSampler);
        admin.getGridServiceContainers().addEventListener(eventSampler);

        Thread.sleep(10000000);
    }

    public void machineAdded(Machine machine) {
        System.out.println("Machine Added [" + machine.getUID() + "]");
    }

    public void machineRemoved(Machine machine) {
        System.out.println("Machine Removed [" + machine.getUID() + "]");
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
        System.out.println("Processing Unit [" + processingUnit.getName() + "] Managing GSM [" + newManaingGridServiceManager.getUID() + "]");
    }

    public void processingUnitManagingGridServiceManagerUnknown(ProcessingUnit processingUnit) {
        System.out.println("Processing Unit [" + processingUnit.getName() + "] Managing GSM UNKNOWN");
    }

    public void processingUnitBackupGridServiceManagerAdded(ProcessingUnit processingUnit, GridServiceManager gridServiceManager) {
        System.out.println("Processing Unit [" + processingUnit.getName() + "] Backup GSM Added [" + gridServiceManager.getUID() + "]");
    }

    public void processingUnitBackupGridServiceManagerRemoved(ProcessingUnit processingUnit, GridServiceManager gridServiceManager) {
        System.out.println("Processing Unit [" + processingUnit.getName() + "] Backup GSM Removed [" + gridServiceManager.getUID() + "]");
    }

    public void gridServiceManagerAdded(GridServiceManager gridServiceManager) {
        System.out.println("GSM Added [" + gridServiceManager.getUID() + "]");
    }

    public void gridServiceManagerRemoved(GridServiceManager gridServiceManager) {
        System.out.println("GSM Removed [" + gridServiceManager.getUID() + "]");
    }

    public void gridServiceContainerAdded(GridServiceContainer gridServiceContainer) {
        System.out.println("GSC Added [" + gridServiceContainer.getUID() + "]");
    }

    public void gridServiceContainerRemoved(GridServiceContainer gridServiceContainer) {
        System.out.println("GSC Removed [" + gridServiceContainer.getUID() + "]");
    }
}
