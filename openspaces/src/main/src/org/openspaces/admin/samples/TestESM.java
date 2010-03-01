package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;
import org.openspaces.admin.esm.deployment.IsolationLevel;
import org.openspaces.admin.esm.deployment.MemorySla;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;

public class TestESM {
 
        public static void main(String[] args) throws Exception {
            
            Admin admin = new AdminFactory().addGroup("moran-gigaspaces-7.1.0-XAPPremium-rc").createAdmin();
            System.out.println("Waiting for at least one agent");
            GridServiceAgent gridServiceAgent = admin.getGridServiceAgents().waitForAtLeastOne();
            System.out.println("found gsa");
            ElasticServiceManager elasticServiceManager = admin.getElasticServiceManagers().waitForAtLeastOne();

            System.out.println("found esm, deploying");
            ProcessingUnit pu = elasticServiceManager.
            deploy(new ElasticDataGridDeployment("grid").isolationLevel(
                    IsolationLevel.DEDICATED).elasticity("2g", "6g").maximumJavaHeapSize("1g").addSla(new MemorySla(40)));
            
            System.out.println("PU deployed: " + pu.getName() + " " + pu.getNumberOfInstances()+","+pu.getNumberOfBackups());
            System.exit(0);
        }
}
