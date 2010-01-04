package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;
import org.openspaces.admin.esm.deployment.IsolationLevel;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.esm.MemorySettings;

public class TestESM {
    
        public static void main__(String[] args) {
            int initialNumberOfJvms = MemorySettings.valueOf("1GB").dividedBy("512MB");
            int maxNumberOfJvms = MemorySettings.valueOf("2GB").dividedBy("512MB");
            int maxNumberOfPartitions = maxNumberOfJvms;
            
            boolean highlyAvailable = true;
            if (highlyAvailable) {
                initialNumberOfJvms *=2;
                maxNumberOfJvms *= 2;
            }
            
            int maxInstancesPerJvm = maxNumberOfJvms/initialNumberOfJvms;
            if (maxInstancesPerJvm > 10) {
                //..limit to 10
                initialNumberOfJvms = maxNumberOfJvms/10;
                maxInstancesPerJvm = maxNumberOfJvms/initialNumberOfJvms;
            }
            
            System.out.println("JVMs: " + initialNumberOfJvms + "-" + maxNumberOfJvms);
            System.out.println("partitions: " + maxNumberOfPartitions+",1");
            System.out.println("max instances per JVM: " + maxInstancesPerJvm);
        }
    
        public static void main(String[] args) {
            Admin admin = new AdminFactory().addGroup("moran-gigaspaces-7.1.0-XAPPremium-m5").createAdmin();
            System.out.println("Waiting for at least one agent");
            GridServiceAgent gridServiceAgent = admin.getGridServiceAgents().waitForAtLeastOne();
            System.out.println("found gsa");
            ElasticServiceManager elasticServiceManager = admin.getElasticServiceManagers().waitForAtLeastOne();

            System.out.println("found esm");
            ProcessingUnit pu = elasticServiceManager.
            deploy(new ElasticDataGridDeployment("foo").isolationLevel(
                    IsolationLevel.SHARED_PUBLIC).highlyAvailable(true).elasticity("1GB", "2GB"));
            
            System.out.println("PU deployed: " + pu.getName() + " partitions: " + pu.getPartitions().length + " instances: " + pu.getNumberOfInstances() + " total: " + pu.getTotalNumberOfInstances());
        }
}
