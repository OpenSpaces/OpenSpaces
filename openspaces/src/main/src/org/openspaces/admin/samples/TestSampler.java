package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.machine.Machine;

/**
 * @author kimchy
 */
public class TestSampler {

    public static void main(String[] args) throws InterruptedException {
        Admin admin = new AdminFactory().addGroup("kimchy").getAdmin();
        admin.start();
        while (true) {
            try {
                for (LookupService lookupService : admin.getLookupServices().getLookupServices()) {
                    System.out.println("Lookup [" + lookupService.getUID() + "] : " + lookupService.getTransport().getConfiguration().getPort());
                }
                for (GridServiceManager gridServiceManager : admin.getGridServiceManagers()) {
                    System.out.println("GSM [" + gridServiceManager.getUID() + "] : " + gridServiceManager.getTransport().getStatistics().getActiveThreadsCount());
                }
                for (GridServiceContainer gridServiceContainer : admin.getGridServiceContainers()) {
                    System.out.println("GSC [" + gridServiceContainer.getUID() + "] : " + gridServiceContainer.getTransport().getStatistics().getActiveThreadsCount());
                }
                for (Machine machine : admin.getMachines().getMachines()) {
                    System.out.println("Machine [" + machine.getUID() + "], transports: " + machine.getTransports().size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }
    }
}
