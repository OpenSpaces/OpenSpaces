package org.openspaces.grid.esm;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.space.SpaceDeployment;

public class InternalESMImpl {
    private final Admin admin = new AdminFactory().addGroup("moran-gigaspaces-7.1.0-XAPPremium-m5").createAdmin();
    
    public InternalESMImpl() {
        
    }

    public void deploy(ElasticDataGridDeployment deployment) {
        SpaceDeployment spaceDeployment = new SpaceDeployment(deployment.getDataGridName());
        int initialNumberOfJvms = MemorySettings.valueOf(deployment.getMinMemory()).dividedBy(deployment.getJvmSize());
        int maxNumberOfJvms = MemorySettings.valueOf(deployment.getMaxMemory()).dividedBy(deployment.getJvmSize());
        int maxNumberOfPartitions = maxNumberOfJvms;
        
        boolean highlyAvailable = true;
        if (highlyAvailable) {
            initialNumberOfJvms *=2;
            maxNumberOfJvms *= 2;
            spaceDeployment.partitioned(maxNumberOfPartitions, 1);
        } else {
            spaceDeployment.partitioned(maxNumberOfPartitions, 0);
        }
        
        int maxInstancesPerJvm = maxNumberOfJvms/initialNumberOfJvms;
        if (maxInstancesPerJvm > 10) {
            //..limit to 10
            initialNumberOfJvms = maxNumberOfJvms/10;
            maxInstancesPerJvm = maxNumberOfJvms/initialNumberOfJvms;
        }
        
        /*
         * On which machine can I load a GSC? 
         *  - exclude any machine with dedicated-private
         *  - exclude any machine with shared-private belonging to another tenant
         */
        admin.getGridServiceAgents().waitForAtLeastOne();
        int numberOfStartedJvms = 0;
        while (numberOfStartedJvms < initialNumberOfJvms) {
            //choose least active agent
            GridServiceAgent agentWithMinGSCs = null;
            for (GridServiceAgent agent : admin.getGridServiceAgents()) {
                if (agentWithMinGSCs == null || agent.getMachine().getGridServiceContainers().getSize() < 
                        agentWithMinGSCs.getMachine().getGridServiceContainers().getSize() ) {
                    agentWithMinGSCs = agent;
                }
            }
            agentWithMinGSCs.startGridService(new GridServiceContainerOptions().vmInputArgument("-Dcom.gs.zones="+deployment.getDataGridName()));
            numberOfStartedJvms++;
        }
        //wait for all GSCs started on this zone
        final String zone = deployment.getDataGridName();
        while (admin.getZones() == null || admin.getZones().getByName(zone) == null) {
            System.out.println("zone not yet discovered - admin.getZones()=" + admin.getZones());
        }
        admin.getZones().getByName(zone).getGridServiceContainers().waitFor(initialNumberOfJvms);
        admin.getGridServiceManagers().deploy(spaceDeployment);
    }
}
