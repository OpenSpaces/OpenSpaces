package org.openspaces.grid.esm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openspaces.admin.Admin;
import org.openspaces.admin.esm.deployment.DeploymentIsolationLevel;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.zone.Zone;

/**
 * Internal deployment isolation filter corresponding to the deployment isolation level required.
 * Filters machines which do not meet the isolation level required to start a new GSC instance to
 * occupy a processing unit instance.
 */
public class DeploymentIsolationFilter {
    
    private final static Logger logger = Logger.getLogger("org.openspaces.grid.esm");
    
    private final Filter filter;
    private final PuCapacityPlanner puCapacityPlanner;
    
    private interface Filter {
        public boolean accept(Machine machine);
    }

    /** @see DeploymentIsolationLevel#DEDICATED */
    private class DedicatedIsolationFilter implements Filter {

        // requires this machine to contain only GSCs matching the zone name provided
        public boolean accept(Machine machine) {
            final String zoneName = puCapacityPlanner.getContextProperties().getZoneName();
            for (GridServiceContainer gsc : machine.getGridServiceContainers()) {
                Map<String, Zone> gscZones = gsc.getZones();
                if (gscZones.isEmpty() || !gscZones.containsKey(zoneName)) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("Can't start GSC on machine [" + ToStringHelper.machineToString(machine)
                                + "] - doesn't meet dedicated deployment isolation constraint.");
                    }
                    return false; // GSC either has no zone or is of another zone
                }
            }

            return true;
        }
    }
    
    /** @see DeploymentIsolationLevel#PUBLIC */
    private class PublicIsolationFilter implements Filter {

        //required this machine to contain only GSCs belonging to zones of public isolation level
        public boolean accept(Machine machine) {

            Admin admin = machine.getGridServiceAgent().getAdmin();
            ProcessingUnits processingUnits = admin.getProcessingUnits();
            // find non-public zones
            HashSet<String> nonPublicZones = new HashSet<String>();
            for (ProcessingUnit pu : processingUnits) {
                ElasticDeploymentContextProperties props = new ElasticDeploymentContextProperties(pu.getBeanLevelProperties().getContextProperties());
                if (!props.isElastic()) continue;
                
                DeploymentIsolationLevel deploymentIsolationLevel = DeploymentIsolationLevel.valueOf(props.getDeploymentIsolationLevel());
                if (!deploymentIsolationLevel.equals(DeploymentIsolationLevel.PUBLIC)) {
                    String puZone = props.getZoneName();
                    nonPublicZones.add(puZone);
                }
            }

            GridServiceContainers gridServiceContainers = machine.getGridServiceContainers();
            for (GridServiceContainer gsc : gridServiceContainers) {
                Map<String, Zone> zones = gsc.getZones();
                for (String zoneName : zones.keySet()) {
                    if (nonPublicZones.contains(zoneName)) {
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("Can't start GSC on machine [" + ToStringHelper.machineToString(machine)
                                    + "] - doesn't meet public deployment isolation constraint.");
                        }
                        return false;
                    }
                }
            }

            return true;
        }
    }
    
    /** @see DeploymentIsolationLevel#SHARED */
    private class SharedIsolationFilter implements Filter {

        //machine can be shared with other processing units belonging to this tenant
        //requires this machine to contain only GSCs with zones of this tenant name
        public boolean accept(Machine machine) {
            final String tenant = puCapacityPlanner.getContextProperties().getTenant();
            for (GridServiceContainer gsc : machine.getGridServiceContainers()) {

                boolean meetsIsolation = true;
                Map<String, Zone> gscZones = gsc.getZones();
                for (String gscZoneName : gscZones.keySet()) {
                    if (!gscZoneName.startsWith(tenant)) {
                        meetsIsolation = false;
                        break;
                    }
                }
                
                if (!meetsIsolation || gscZones.isEmpty()) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("Can't start GSC on machine [" + ToStringHelper.machineToString(machine)
                                + "] - doesn't meet shared deployment isolation constraint.");
                    }
                    return false;
                }
            }

            return true;
        }
        
    }
    
    public DeploymentIsolationFilter(PuCapacityPlanner puCapacityPlanner) {
        this.puCapacityPlanner = puCapacityPlanner;
        DeploymentIsolationLevel deploymentIsolationLevel = DeploymentIsolationLevel.valueOf(puCapacityPlanner.getContextProperties().getDeploymentIsolationLevel());
        Filter ifilter = null;
        switch(deploymentIsolationLevel) {
            case DEDICATED:
                ifilter = new DedicatedIsolationFilter();
                break;
            case PUBLIC:
                ifilter = new PublicIsolationFilter();
                break;
            case SHARED:
                ifilter = new SharedIsolationFilter();
                break;
            default:
                throw new IllegalStateException("Unknown deployment isolation level ["+deploymentIsolationLevel+"]");
        }
        
        this.filter = ifilter;
    }
    
    public List<Machine> filter(List<Machine> machines) {
        List<Machine> filtered = new ArrayList<Machine>(machines.size());
        for (Machine machine : machines) {
            if (filter.accept(machine))
                filtered.add(machine);
        }
        return filtered;
    }
    
}
