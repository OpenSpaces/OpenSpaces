package org.openspaces.grid.esm;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.admin.zone.Zone;

public class InternalESMImpl {
    private final static Logger logger = Logger.getLogger("org.openspaces.grid.esm");
    private final Admin admin = new AdminFactory().addGroup("moran-gigaspaces-7.1.0-XAPPremium-m5").createAdmin();
    
    public InternalESMImpl() {
        logger.setLevel(Level.ALL);
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
        if (maxInstancesPerJvm > deployment.getMaxInstancesPerJvm()) {
            initialNumberOfJvms = maxNumberOfJvms/deployment.getMaxInstancesPerJvm();
            maxInstancesPerJvm = maxNumberOfJvms/initialNumberOfJvms;
        }
        
        final String zone = deployment.getDataGridName();
        
        logger.fine(
                "\n initial number of JVMs: " + initialNumberOfJvms +
                "\n max number of JVMs: " + maxNumberOfJvms +
                "\n max instances per JVM: " + maxInstancesPerJvm +
                "\n topology: " + maxNumberOfPartitions + (highlyAvailable?",1":"") +
                "\n zone: " + zone
        );
        
 
        /*
         * Start GSCs on agents meeting isolation level
         */
        admin.getGridServiceAgents().waitForAtLeastOne();
        int numberOfStartedJvms = 0;
        while (numberOfStartedJvms < initialNumberOfJvms) {
            //choose least active agent
            GridServiceAgent agentWithMinGSCs = null;
            for (GridServiceAgent agent : admin.getGridServiceAgents()) {

                /*
                 * Dedicated isolation, requires this agent to contain only GSCs matching this zone
                 */
                boolean meetsIsolation = true;
                for (GridServiceContainer gsc : agent.getMachine().getGridServiceContainers()) {
                    if (!agent.equals(gsc.getGridServiceAgent())) {
                        continue; //container not loaded from this agent (i.e., multiple agents on same machine!)
                    }
                    Map<String, Zone> gscZones = gsc.getZones();
                    if (!gscZones.containsKey(zone)) {
                        //GSC is of another zone
                        meetsIsolation = false;
                        break;
                    }
                }
                
                if (!meetsIsolation) {
                    continue; //skip agent
                }

                if (agentWithMinGSCs == null || agent.getMachine().getGridServiceContainers().getSize() < 
                        agentWithMinGSCs.getMachine().getGridServiceContainers().getSize() ) {
                    agentWithMinGSCs = agent;
                }
            }
            if (agentWithMinGSCs == null) {
                throw new RuntimeException("Unavailable resource");
            }
            agentWithMinGSCs.startGridService(new GridServiceContainerOptions().vmInputArgument("-Dcom.gs.zones="+deployment.getDataGridName()));
            numberOfStartedJvms++;
        }
        //wait for all GSCs started on this zone
        
        Zone zoneByName = admin.getZones().waitFor(zone);
        zoneByName.getGridServiceContainers().waitFor(initialNumberOfJvms);
        admin.getGridServiceManagers().deploy(spaceDeployment);
    }
}
