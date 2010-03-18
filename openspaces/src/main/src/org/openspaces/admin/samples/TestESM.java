package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;
import org.openspaces.admin.esm.deployment.MemorySla;
import org.openspaces.admin.pu.ProcessingUnit;

public class TestESM {
 
        public static void main(String[] args) throws Exception {
            
            Admin admin = new AdminFactory().addGroup("moran-gigaspaces-7.1.0-XAPPremium-rc").createAdmin();
            System.out.println("Waiting for at least one agent");
            admin.getGridServiceAgents().waitForAtLeastOne();
            System.out.println("found gsa");
            ElasticServiceManager elasticServiceManager = admin.getElasticServiceManagers().waitForAtLeastOne();

            System.out.println("found esm, deploying");
            
            ProcessingUnit pu = elasticServiceManager.
            deploy(new ElasticDataGridDeployment("mygrid")
            .elasticity("10m", "100m")
            .maximumJavaHeapSize("1g")
            .addSla(new MemorySla("40%"))
//            .elasticScaleConfig(new ElasticScaleConfig(PcLabOnDemandElasticScale.class.getName()).addProperty("machines", "pc-lab12,pc-lab13"))
            
            );
            
            System.out.println("PU deployed: " + pu.getName() + " " + pu.getNumberOfInstances()+","+pu.getNumberOfBackups());
            System.exit(0);
        }
}
