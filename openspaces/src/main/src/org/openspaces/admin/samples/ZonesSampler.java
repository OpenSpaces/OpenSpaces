package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.zone.Zone;

/**
 * @author kimchy
 */
public class ZonesSampler {

    public static void main(String[] args) throws InterruptedException {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        while(true) {
            for (Zone zone : admin.getZones()) {
                System.out.println("Zone [" + zone.getName() + "]");
                System.out.println("    -> Spaces [" + zone.getSpaceInstances().length + "], PUs [" + zone.getProcessingUnitInstances().length + "], GSA [" + zone.getGridServiceAgents().getSize() + "], GSMs [" + zone.getGridServiceManagers().getSize() + "], GSCs [" + zone.getGridServiceContainers().getSize() + "], Vms [" + zone.getVirtualMachines().getSize() + "], Transports [" + zone.getTransports().getSize() + "], Machines [" + zone.getMachines().getSize() + "]");
            }
            Thread.sleep(1000);
        }
    }
}
