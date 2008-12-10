package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsa.GridServiceManagerOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceDeployment;

/**
 * @author kimchy
 */
public class TestAgent {

    public static void main(String[] args) throws Exception {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        System.out.println("Waiting for at least one agent");
        GridServiceAgent gridServiceAgent = admin.getGridServiceAgents().waitForAtLeastOne();

        System.out.println("Starting 2 GSM and 2 GSC");

        GridServiceManager gridServiceManager = gridServiceAgent.startGridServiceAndWait(new GridServiceManagerOptions().useScript());
        gridServiceAgent.startGridService(new GridServiceManagerOptions());

        GridServiceContainer gsc1 = gridServiceAgent.startGridServiceAndWait(new GridServiceContainerOptions().addInputParameter("-Xmx512m"));
        GridServiceContainer gsc2 = gridServiceAgent.startGridServiceAndWait(new GridServiceContainerOptions().useScript());

        System.out.println("Deploying a space");
        ProcessingUnit processingUnit = gridServiceManager.deploy(new SpaceDeployment("test").numberOfInstances(2).numberOfBackups(1));
        Space space = processingUnit.waitForSpace();
        System.out.println("Waiting for all instances to be up");
        space.waitFor(4);

        System.out.println("Undeploying the space");
        processingUnit.undeploy();
        System.out.println("Stopping GSM and GSC");

        gsc1.kill();
        gsc2.kill();
        gridServiceManager.kill();

        admin.close();
    }
}
