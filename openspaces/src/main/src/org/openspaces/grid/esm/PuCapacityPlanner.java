package org.openspaces.grid.esm;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.zone.Zone;

public class PuCapacityPlanner {
    private final ProcessingUnit pu;
    private final int scalingFactor;
    private final int minNumberOfGSCs;
    private final int maxNumberOfGSCs;
    private int maxNumberOfGSCsPerMachine;
    private final String zoneName;
    private final String maxJavaHeapSize;
    private final String initJavaHeapSize;
    private final OnDemandElasticScale elasticScale;

    public PuCapacityPlanner(ProcessingUnit pu, OnDemandElasticScale elasticScale) {
        this.pu = pu;
        this.elasticScale = elasticScale;
        
        Properties contextProperties = pu.getBeanLevelProperties().getContextProperties();

        initJavaHeapSize = contextProperties.getProperty("initialJavaHeapSize");
        maxJavaHeapSize = contextProperties.getProperty("maximumJavaHeapSize");
        
        minNumberOfGSCs = MemorySettings.valueOf(contextProperties.getProperty("minMemory")).floorDividedBy(maxJavaHeapSize);
        maxNumberOfGSCs = MemorySettings.valueOf(contextProperties.getProperty("maxMemory")).floorDividedBy(maxJavaHeapSize);
        
        scalingFactor = (int)Math.ceil(1.0*maxNumberOfGSCs / minNumberOfGSCs);
        
        int partitions = pu.getNumberOfInstances();
        maxNumberOfGSCsPerMachine = (int)Math.ceil(1.0*partitions / scalingFactor);

        zoneName = pu.getRequiredZones()[0];
    }
    
    public static boolean isElastic(ProcessingUnit pu){
        return (pu.getBeanLevelProperties().getContextProperties().containsKey("elastic"));
    }
    
    public int getMaxNumberOfGSCsPerMachine() {
        return maxNumberOfGSCsPerMachine;
    }

    public void setMaxNumberOfGSCsPerMachine(int maxNumberOfGSCsPerMachine) {
        this.maxNumberOfGSCsPerMachine = maxNumberOfGSCsPerMachine;
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

    public String getZoneName() {
        return zoneName;
    }
    
    public String getInitJavaHeapSize() {
        return initJavaHeapSize;
    }
    
    public String getMaxJavaHeapSize() {
        return maxJavaHeapSize;
    }

    public ProcessingUnit getProcessingUnit() {
        return pu;
    }
    
    public OnDemandElasticScale getElasticScale() {
        return elasticScale;
    }
    
    public int getNumberOfGSCsInZone() {
        Zone zone = pu.getAdmin().getZones().getByName(zoneName);
        return zone == null ? 0 : zone.getGridServiceContainers().getSize();
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
        Zone zone = pu.getAdmin().getZones().getByName(zoneName);
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
}
