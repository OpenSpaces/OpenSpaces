package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;
import org.openspaces.admin.esm.deployment.IsolationLevel;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.zone.Zone;
import org.openspaces.grid.esm.InternalESMImpl;
import org.openspaces.grid.esm.MemorySettings;

public class TestESM {
    
        public static void main__00(String[] args) {
            
            boolean highlyAvailable = true;
            int availabilityFactor = highlyAvailable ? 2 : 1;
            String min = "32GB"; //"111GB"; //"64GB";
            String max = "320GB"; //"1600GB"; //"320GB";
            String machineRAMSize = "32GB";
            String jvmSize = "2GB"; //32Bit = 2GB, 64Bit = 6GB
            int scalingGrowth = 4;
            
            System.out.println("Input: ");
            System.out.println("min="+min + " max="+max);
            System.out.println("Machine RAM size: " + machineRAMSize);
            System.out.println("JVM Size: " + jvmSize);
            System.out.println("Scaling Growth: " + scalingGrowth);
            System.out.println("--");
            
            int numberOfMachinesNeeded = MemorySettings.valueOf(min).ceilDividedBy(machineRAMSize);
            System.out.println("Initial number of machines: " + numberOfMachinesNeeded + " (" + (numberOfMachinesNeeded*availabilityFactor) + " with backups)");
            
            int numberOfGSCsPerMachine = MemorySettings.valueOf(machineRAMSize).floorDividedBy(jvmSize);
            System.out.println("GSCs per machine: " + numberOfGSCsPerMachine);
            
            int initialTotalNumberOfGSCs = numberOfMachinesNeeded * numberOfGSCsPerMachine;
            System.out.println("Initial total number of GSCs: " + initialTotalNumberOfGSCs + " (" + (initialTotalNumberOfGSCs*availabilityFactor) + " with backups)");
            
            System.out.println("--");
            
            //1.6 TB
            int maxNumberOfMachinesNeeded = MemorySettings.valueOf(max).ceilDividedBy(machineRAMSize);
            System.out.println("Max number of machines: " + maxNumberOfMachinesNeeded + " (" + (maxNumberOfMachinesNeeded*availabilityFactor) + " with backups)");
            
            int maxTotalNumberOfGSCs = maxNumberOfMachinesNeeded * numberOfGSCsPerMachine;
            System.out.println("Max total number of GSCs: " + maxTotalNumberOfGSCs + " (" + (maxTotalNumberOfGSCs*availabilityFactor) + " with backups)");
            
            System.out.println("--");
            int partitions = initialTotalNumberOfGSCs*scalingGrowth;
            System.out.println("Partitions: " + partitions + " ("+partitions+",1 with backups)");
        }
        
        public static void main_999(String[] args) {
            
            //main__00(args);
            
            boolean highlyAvailable = true;
            int initialNumberOfJvms = MemorySettings.valueOf("32GB").floorDividedBy("6GB");
            int maxNumberOfJvms = MemorySettings.valueOf("320GB").floorDividedBy("6GB");
            int maxNumberOfPartitions = maxNumberOfJvms;
            
            if (highlyAvailable) {
                initialNumberOfJvms *=2;
                maxNumberOfJvms *= 2;
            }
            
            int maxInstancesPerJvm = maxNumberOfJvms/initialNumberOfJvms;
            if (maxInstancesPerJvm > 4) {
                //..limit to 10
                initialNumberOfJvms = maxNumberOfJvms/4;
                maxInstancesPerJvm = maxNumberOfJvms/initialNumberOfJvms;
            }
            
            System.out.println("JVMs: " + initialNumberOfJvms + "-" + maxNumberOfJvms);
            System.out.println("partitions: " + maxNumberOfPartitions+",1");
            System.out.println("max instances per JVM: " + maxInstancesPerJvm);
        }
    
        
        public static void main__1(String[] args) throws Exception {
            Admin admin = new AdminFactory().addGroup("moran-gigaspaces-7.1.0-XAPPremium-m5").createAdmin();
            while (true) {
                System.out.println("zones = " + admin.getZones().getNames().keySet());
                Thread.sleep(3000);
                Zone byName = admin.getZones().getByName("moran-gigaspaces-7.1.0-XAPPremium-m5");
                if (byName != null) {
                    System.out.println("agents: " + byName.getGridServiceAgents().getSize());
                }
            }
        }
        
        public static void main(String[] args) throws Exception {
            Admin admin = new AdminFactory().addGroup("moran-gigaspaces-7.1.0-XAPPremium-m5").createAdmin();
            System.out.println("Waiting for at least one agent");
            GridServiceAgent gridServiceAgent = admin.getGridServiceAgents().waitForAtLeastOne();
            System.out.println("found gsa");
            ElasticServiceManager elasticServiceManager = admin.getElasticServiceManagers().waitForAtLeastOne();

            System.out.println("found esm");
            ProcessingUnit pu = elasticServiceManager.
            deploy(new ElasticDataGridDeployment("barfoo").isolationLevel(
                    IsolationLevel.DEDICATED).highlyAvailable(true).elasticity("2GB", "10GB"));
            
            System.out.println("PU deployed: " + pu.getName() + " " + pu.getNumberOfInstances()+","+pu.getNumberOfBackups());
        
//            System.out.println("deploy another?");
//            System.in.read();System.in.read();
//        
//            ProcessingUnit pu2 = elasticServiceManager.
//            deploy(new ElasticDataGridDeployment("bar").isolationLevel(
//                    IsolationLevel.PUBLIC).highlyAvailable(true).elasticity("1GB", "1GB"));
//            
//            System.out.println("PU deployed: " + pu2.getName() + " partitions: " + pu2.getPartitions().length + " instances: " + pu.getNumberOfInstances() + " total: " + pu2.getTotalNumberOfInstances());
        }
        
        public static void main_debug(String[] args) throws Exception {
            Admin admin = new AdminFactory().addGroup("moran-gigaspaces-7.1.0-XAPPremium-m5").createAdmin();
            System.out.println("Waiting for at least one agent");
            GridServiceAgent gridServiceAgent = admin.getGridServiceAgents().waitForAtLeastOne();
            System.out.println("found gsa");

            System.out.println("found esm");
            InternalESMImpl elasticServiceManager = new InternalESMImpl();
            elasticServiceManager.
            deploy(new ElasticDataGridDeployment("foo").isolationLevel(
                    IsolationLevel.PUBLIC).highlyAvailable(true).elasticity("1GB", "1GB"));
            
            System.out.println("deploy another?");
            System.in.read();System.in.read();
        
            elasticServiceManager.
            deploy(new ElasticDataGridDeployment("bar").isolationLevel(
                    IsolationLevel.PUBLIC).highlyAvailable(true).elasticity("1GB", "1GB"));

        }
}
