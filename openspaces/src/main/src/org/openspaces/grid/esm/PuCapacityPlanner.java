package org.openspaces.grid.esm;

import java.util.Properties;

import org.openspaces.admin.pu.ProcessingUnit;

public class PuCapacityPlanner {
    private final ProcessingUnit pu;
    private final int scalingFactor;
    private final int minNumberOfGSCs;
    private final int maxNumberOfGSCs;
    private int maxNumberOfGSCsPerMachine;
    private final String zoneName;

    
    
    /*
     * 
     * 3G-10G, 512MB
     * max-jvms= 10GB / 512 MB = 20
     * min-jvms= 3GB / 512 MB = 6
     * partitions = 20
     * highly available? partitions/2 = 10,1
     * scale factor = 20/6 = 3
     * initial min-jvms * scale factor = 6*3 = 18 instances
     * total = max-jvms * scale factor = 20*3 = 60 instances 
     * 
     */
    public PuCapacityPlanner(ProcessingUnit pu) {
        this.pu = pu;
        
        Properties contextProperties = pu.getBeanLevelProperties().getContextProperties();

        minNumberOfGSCs = MemorySettings.valueOf(contextProperties.getProperty("minMemory")).floorDividedBy(
                contextProperties.getProperty("jvmSize"));

        maxNumberOfGSCs = MemorySettings.valueOf(contextProperties.getProperty("maxMemory")).floorDividedBy(
                contextProperties.getProperty("jvmSize"));
        
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

    public ProcessingUnit getProcessingUnit() {
        return pu;
    }
}
