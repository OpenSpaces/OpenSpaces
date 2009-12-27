package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.gsa.ElasticServiceManagerOptions;
import org.openspaces.admin.gsa.GridServiceAgent;


/**
 * @author kimchy
 */
public class TestAgent {

    public static void main(String[] args) throws Exception {
        Admin admin = new AdminFactory().addGroup("moran-gigaspaces-7.1.0-XAPPremium-m5").createAdmin();
        System.out.println("Waiting for at least one agent");
        GridServiceAgent gridServiceAgent = admin.getGridServiceAgents().waitForAtLeastOne();

        ElasticServiceManager elasticServiceManager = gridServiceAgent.startGridServiceAndWait(new ElasticServiceManagerOptions());
        System.out.println(elasticServiceManager.getVirtualMachine().getMachine().getHostAddress());
        
//        System.out.println("Starting 2 GSM and 2 GSC");
//
//        GridServiceManager gsm1 = gridServiceAgent.startGridServiceAndWait(new GridServiceManagerOptions().useScript());
//        GridServiceManager gsm2 = gridServiceAgent.startGridServiceAndWait(new GridServiceManagerOptions());
//
//        GridServiceContainer gsc1 = gridServiceAgent.startGridServiceAndWait(new GridServiceContainerOptions().vmInputArgument("-Xmx512m"));
//        GridServiceContainer gsc2 = gridServiceAgent.startGridServiceAndWait(new GridServiceContainerOptions().useScript());
//
//        gridServiceAgent.startGridService(new GridServiceOptions("hsqldb").argument("-port").argument("1987"));
//
//        System.out.println("Deploying a space");
//        ProcessingUnit processingUnit = gsm1.deploy(new SpaceDeployment("test").numberOfInstances(2).numberOfBackups(1));
//        Space space = processingUnit.waitForSpace();
//        System.out.println("Waiting for all instances to be up");
//        space.waitFor(4);
//
//        System.out.println("Undeploying the space");
//        processingUnit.undeploy();
//        System.out.println("Stopping GSM and GSC");
//
//        gsc1.kill();
//        gsc2.kill();
//        gsm1.kill();
//        gsm2.kill();
//
//        admin.close();
    }
}
