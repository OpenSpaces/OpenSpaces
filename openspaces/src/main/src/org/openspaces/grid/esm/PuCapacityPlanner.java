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
    private final String jvmSize;

    public PuCapacityPlanner(ProcessingUnit pu) {
        this.pu = pu;
        
        Properties contextProperties = pu.getBeanLevelProperties().getContextProperties();

        jvmSize = contextProperties.getProperty("jvmSize");
        
        minNumberOfGSCs = MemorySettings.valueOf(contextProperties.getProperty("minMemory")).floorDividedBy(jvmSize);
        maxNumberOfGSCs = MemorySettings.valueOf(contextProperties.getProperty("maxMemory")).floorDividedBy(jvmSize);
        
        scalingFactor = (int)Math.ceil(1.0*maxNumberOfGSCs / minNumberOfGSCs);
        
        int partitions = pu.getNumberOfInstances();
        maxNumberOfGSCsPerMachine = (int)Math.ceil(1.0*partitions / scalingFactor);

        zoneName = pu.getRequiredZones()[0];
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
    
    public String getJvmSize() {
        return jvmSize;
    }

    public ProcessingUnit getProcessingUnit() {
        return pu;
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
        
        Zone zone = pu.getAdmin().getZones().getByName(zoneName);
        if (zone == null) return false;
        int nOfMachines = zone.getMachines().getSize();
        if (nOfMachines <= 1) {
            return false; //fast exit
        }
        Set<Machine> machines = new HashSet<Machine>(nOfMachines);
        GridServiceContainers gscsInZone = zone.getGridServiceContainers();
        for (GridServiceContainer gsc : gscsInZone) {
            machines.add(gsc.getMachine());
        }
        
        return (machines.size() > 1);
    }
}
