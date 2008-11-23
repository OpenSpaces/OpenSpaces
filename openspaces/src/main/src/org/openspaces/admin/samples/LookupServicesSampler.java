package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.LookupService;
import org.openspaces.admin.Machine;

/**
 * @author kimchy
 */
public class LookupServicesSampler {

    public static void main(String[] args) throws InterruptedException {
        Admin admin = new AdminFactory().addGroup("kimchy").getAdmin();
        admin.start();
        while (true) {
            for (LookupService lookupService : admin.getLookupServices().getLookupServices()) {
                System.out.println("Lookup [" + lookupService.getUID() + "] : " + lookupService.getTransportStatistics().getActiveThreadsCount());
            }
            for (Machine machine : admin.getMachines().getMachines()) {
                System.out.println("Machine [" + machine.getUID() + "]");
            }
            Thread.sleep(200);
        }
    }
}
