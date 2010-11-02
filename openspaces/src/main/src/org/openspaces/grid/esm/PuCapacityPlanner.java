package org.openspaces.grid.esm;

import java.util.HashSet;
import java.util.Set;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.ElasticDeploymentContextProperties;
import org.openspaces.admin.zone.Zone;

public class PuCapacityPlanner {
    private final ProcessingUnit pu;
    private final int scalingFactor;
    private final int minNumberOfGSCs;
    private final int maxNumberOfGSCs;
    private final ElasticScaleHandler elasticScale;
    private final DeploymentIsolationFilter deploymentIsolationFilter;

    private final ElasticDeploymentContextProperties contextProperties;
    
    public PuCapacityPlanner(ProcessingUnit pu, ElasticScaleHandler elasticScale) {
        this.pu = pu;
        this.elasticScale = elasticScale;
        
        contextProperties = new ElasticDeploymentContextProperties(pu.getBeanLevelProperties().getContextProperties());

        String initialJavaHeapSize = contextProperties.getInitialJavaHeapSize();
        String maximumJavaHeapSize = contextProperties.getMaximumJavaHeapSize();
        if (MemorySettings.valueOf(initialJavaHeapSize).isGreaterThan(MemorySettings.valueOf(maximumJavaHeapSize))) {
            initialJavaHeapSize = maximumJavaHeapSize;
        }
        
        minNumberOfGSCs = Math.max(1, MemorySettings.valueOf(contextProperties.getMinMemoryCapacity()).floorDividedBy(contextProperties.getMaximumJavaHeapSize()));
        maxNumberOfGSCs = Math.max(1, MemorySettings.valueOf(contextProperties.getMaxMemoryCapacity()).floorDividedBy(contextProperties.getMaximumJavaHeapSize()));
        
        int scaleGrowth = (int)(1.0*pu.getTotalNumberOfInstances() / minNumberOfGSCs);
        int scaleFactor = (int)Math.ceil(1.0*maxNumberOfGSCs / minNumberOfGSCs);
        scalingFactor = Math.max(scaleGrowth, scaleFactor);
        
        deploymentIsolationFilter = new DeploymentIsolationFilter(this);
    }
    
    public static boolean isElastic(ProcessingUnit pu){
        return (pu.getBeanLevelProperties().getContextProperties().containsKey(ElasticDeploymentContextProperties.ELASTIC));
    }
    
    public int getScalingFactor() {
        return scalingFactor;
    }

    public int getMinNumberOfGSCs() {
        return minNumberOfGSCs;
    }

    public int getMaxNumberOfGSCs() {
        return maxNumberOfGSCs;
    }

    public ProcessingUnit getProcessingUnit() {
        return pu;
    }
    
    public ElasticScaleHandler getElasticScale() {
        return elasticScale;
    }
    
    public int getNumberOfGSCsInZone() {
        Zone zone = pu.getAdmin().getZones().getByName(contextProperties.getZoneName());
        return zone == null ? 0 : zone.getGridServiceContainers().getSize();
    }
    
    public int getNumberOfNonEmptyGSCsInZone() {
        Zone zone = pu.getAdmin().getZones().getByName(contextProperties.getZoneName());
        if (zone == null)
            return 0;
        
        int numberOfNonEmptyGSCsInZoneCount = 0;
        for (GridServiceContainer gsc : zone.getGridServiceContainers()) {
            if (gsc.getProcessingUnitInstances().length > 0) {
                ++numberOfNonEmptyGSCsInZoneCount;
            }
        }
        
        return numberOfNonEmptyGSCsInZoneCount;
    }
    
    public int getNumberOfMachinesWithEmptyGSCsInZone() {
        Zone zone = pu.getAdmin().getZones().getByName(contextProperties.getZoneName());
        if (zone == null)
            return 0;
        
        int numberOfMachinesWithEmptyGSC = 0;
        Set<Machine> machines = new HashSet<Machine>();
        GridServiceContainers gscsInZone = zone.getGridServiceContainers();
        for (GridServiceContainer gsc : gscsInZone) {
            if (machines.contains(gsc.getMachine())) {
                continue;
            }
            machines.add(gsc.getMachine());
            if (gsc.getProcessingUnitInstances().length == 0) {
                ++numberOfMachinesWithEmptyGSC;
            }
        }
        
        return numberOfMachinesWithEmptyGSC;
    }

    /**
     * We are looking to satisfy the max-instances-per-machine SLA.
     * If there are more than 1 machine in the zone then the SLA is obeyed.
     */
    public boolean hasEnoughMachines() {
        if (pu.getMaxInstancesPerMachine() < 1) {
            return true;
        }
        
        int numberOfMachinesInZone = getNumberOfMachinesInZone();
        return (numberOfMachinesInZone > 1);
    }
    
    public int getNumberOfMachinesInZone() {
        Zone zone = pu.getAdmin().getZones().getByName(contextProperties.getZoneName());
        if (zone == null) return 0;

        //will include only machines which have gscs (and probably pus)
        //an empty machine (no gscs) will not be included
        Set<Machine> machines = new HashSet<Machine>();
        GridServiceContainers gscsInZone = zone.getGridServiceContainers();
        for (GridServiceContainer gsc : gscsInZone) {
            machines.add(gsc.getMachine());
        }
        
        return (machines.size());
    }
    
    
    
    public ElasticDeploymentContextProperties getContextProperties() {
        return contextProperties;
    }
    
    public DeploymentIsolationFilter getDeploymentIsolationFilter() {
        return deploymentIsolationFilter;
    }
    
    public boolean isProcessingUnitIntact() {
        return isProcessingUnitIntact(pu);
    }
    
    public static boolean isProcessingUnitIntact (ProcessingUnit pu) {
        int planned = pu.getTotalNumberOfInstances();
        int actual = pu.getInstances().length;
        
        return pu.getStatus().equals(DeploymentStatus.INTACT) 
            && planned == actual;
    }
}
