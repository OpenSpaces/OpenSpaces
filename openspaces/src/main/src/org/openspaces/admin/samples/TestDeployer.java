package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.SpaceDeployment;

/**
 * @author kimchy
 */
public class TestDeployer {

    public static void main(String[] args) throws Exception {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        System.out.println("Start 1 GSM and 2 GSCs");
        admin.getGridServiceManagers().waitFor(1);
        System.out.println("Found at least 1 GSM");
        admin.getGridServiceContainers().waitFor(2);
        System.out.println("Found at least 2 GSC");
        ProcessingUnit procesingUnit = admin.getGridServiceManagers().deploy(new SpaceDeployment("test")
                .numberOfInstances(2).maxInstancesPerVM(1).addZone("zone3"));
        System.out.println("Deployed space, waiting...");
        procesingUnit.waitFor(2);
        System.out.println("Deployed space");

        System.out.println("Destroying instnace");
        procesingUnit.getInstances()[0].destroy();
        System.out.println("Waiting again for all processing unit instances to be up");
        procesingUnit.waitFor(2);

        System.out.println("Start another GSC, I am waiting for it!");
        admin.getGridServiceContainers().waitFor(3);
        GridServiceContainer emptyContainer = null;
        for (GridServiceContainer gridServiceContainer : admin.getGridServiceContainers()) {
            if (gridServiceContainer.getProcessingUnitInsances().length == 0) {
                emptyContainer = gridServiceContainer;
                break;
            }
        }
        System.out.println("Relocating....");
        procesingUnit.getInstances()[0].relocate(emptyContainer);
        System.out.println("Waiting for relocation to happen");
        emptyContainer.waitFor(1);
        System.out.println("Relocation happened, hurray!");

        System.out.println("Undeploying...");
        procesingUnit.undeploy();
        System.out.println("Done");
    }
}
