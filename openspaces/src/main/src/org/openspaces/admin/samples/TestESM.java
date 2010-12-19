package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.ElasticDataGridDeployment;

public class TestESM {
 
        public static void main(String[] args) throws Exception {
            
            Admin admin = new AdminFactory().addGroup("moran-gigaspaces-7.1.0-XAPPremium-rc").createAdmin();
            System.out.println("Waiting for at least one agent");
            admin.getGridServiceAgents().waitForAtLeastOne();
            GridServiceManager gsm = admin.getGridServiceManagers().waitForAtLeastOne();
            System.out.println("found gsa,gsm");
            ElasticServiceManager elasticServiceManager = admin.getElasticServiceManagers().waitForAtLeastOne();

            System.out.println("found esm, deploying");
            
            ProcessingUnit pu = gsm.
            deploy(new ElasticDataGridDeployment("mygrid")
            .minMemoryCapacity("1000m")
            .maxMemoryCapacity("2000m")
            .commandLineArgument("-Xmx250m")            
            );
            
            System.out.println("PU deployed: " + pu.getName() + " " + pu.getNumberOfInstances()+","+pu.getNumberOfBackups());
            System.exit(0);
        }
}
