package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.SpaceDeployment;

/**
 * @author kimchy
 */
public class TestDeployer {

    public static void main(String[] args) throws Exception {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        admin.getGridServiceManagers().waitFor(1);
        admin.getGridServiceContainers().waitFor(2);
        System.out.println("Found at least 1 GSM");
        ProcessingUnit procesingUnit = admin.getGridServiceManagers().deploy(new SpaceDeployment("test")
                .numberOfInstances(2).numberOfBackups(1).maxInstancesPerVM(1));
        System.out.println("Deployed space, waiting...");
        procesingUnit.waitFor(4);
        System.out.println("Deployed space");
        System.out.println("Undeploying...");
        procesingUnit.undeploy();
        System.out.println("Done");
    }
}
