package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.pu.service.ProcessingUnitServiceDetails;

/**
 * @author kimchy
 */
public class TestSampler {

    public static void main(String[] args) throws InterruptedException {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        while (true) {
            try {
                for (LookupService lookupService : admin.getLookupServices()) {
                    System.out.println("Lookup [" + lookupService.getUID() + "] : " + lookupService.getVirtualMachine().getMachine().getHost());
                }
                for (GridServiceManager gridServiceManager : admin.getGridServiceManagers()) {
                    System.out.println("GSM [" + gridServiceManager.getUID() + "] : " + gridServiceManager.getOperatingSystem().getUID());
                }
                for (GridServiceContainer gridServiceContainer : admin.getGridServiceContainers()) {
                    System.out.println("GSC [" + gridServiceContainer.getUID() + "] : " + gridServiceContainer.getMachine().getUID());
                    for (ProcessingUnitInstance processingUnitInstance : gridServiceContainer) {
                        System.out.println("   -> PU [" + processingUnitInstance.getClusterInfo() + "]");
                    }
                }
                for (Machine machine : admin.getMachines()) {
                    System.out.println("Machine [" + machine.getUID() + "], transports: " + machine.getOperatingSystem().getUID());
                    for (SpaceInstance spaceInstance : machine.getSpaceInstances()) {
                        System.out.println("   -> Space [" + spaceInstance.getUID() + "]");
                    }
                    for (ProcessingUnitInstance processingUnitInstance : machine.getProcessingUnitInstances()) {
                        System.out.println("   -> PU [" + processingUnitInstance.getUID() + "]");
                    }
                }
                for (ProcessingUnit processingUnit : admin.getProcessingUnits()) {
                    System.out.println("Processing Unit: " + processingUnit.getName() + " status: " + processingUnit.getStatus());
                    if (processingUnit.isManaged()) {
                        System.out.println("   -> Managing GSM: " + processingUnit.getManagingGridServiceManager().getUID());
                    } else {
                        System.out.println("   -> Managing GSM: NA");
                    }
                    for (GridServiceManager backupGSM : processingUnit.getBackupGridServiceManagers()) {
                        System.out.println("   -> Backup GSM: " + backupGSM.getUID());
                    }
                    for (ProcessingUnitInstance processingUnitInstance : processingUnit) {
                        System.out.println("   [" + processingUnitInstance.getClusterInfo() + "] on GSC [" + processingUnitInstance.getGridServiceContainer().getUID() + "]");
                        for (ProcessingUnitServiceDetails details : processingUnitInstance) {
                            System.out.println("      -> Service [" + details.getId() + "] type [" + details.getType() + "]");
                        }
                    }
                }
                for (Space space : admin.getSpaces()) {
                    System.out.println("Space [" + space.getUID() + "] numberOfInstances [" + space.getNumberOfInstances() + "] numberOfbackups [" + space.getNumberOfBackups() + "]");
                    for (SpaceInstance spaceInstance : space) {
                        System.out.println("   -> INSTANCE [" + spaceInstance.getUID() + "] instanceId [" + spaceInstance.getInstanceId() + "] backupId [" + spaceInstance.getBackupId() + "]");
                        System.out.println("         -> Host: " + spaceInstance.getMachine().getHost());
                    }
                }
                System.out.println("*********************************************************************");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }
    }
}
