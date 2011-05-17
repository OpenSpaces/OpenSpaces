package org.openspaces.admin.pu.elastic.config;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.ScaleStrategyConfigUtils;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.strategy.ManualCapacityScaleStrategyBean;

/**
 * Defines a manual scaling strategy that consumes the specified memory capacity.
 * When enabled the processing unit scales out whenever the specified memory capacity deviates from the actual memory capacity.
 * 
 * When a backup partition is enabled (which usually is the case), the specified memory capacity is the total memory occupied by the
 * primary partition instances and the backup partition instances.
 *  
 * @see ManualCapacityScaleConfigurer
 * @since 8.0
 * @author itaif
 */
public class ManualCapacityScaleConfig 
    implements ScaleStrategyConfig , Externalizable {

    private static final long serialVersionUID = 1L;

    
    private static final String STRATEGY_NAME = "scale-strategy.manual-memory";
    private static final String MEMORY_CAPACITY_MEGABYTES_KEY = "memory-capacity-megabytes";
    private static final int MEMORY_CAPACITY_MEGABYTES_DEFAULT = 0;
    private static final String CPU_CAPACITY_CORES_KEY = "cpu-capacity-cores";
    private static final double CPU_CAPACITY_CORES_DEFAULT = 0.0;
    private static final String DRIVE_CAPACITY_MEGABYTES_KEY = "drive-capacity-megabytes";
    private static final String DRIVE_CAPACITY_MEGABYTES_PAIR_SEPERATOR = ",";
    private static final String DRIVE_CAPACITY_MEGABYTES_KEYVALUE_SEPERATOR = "=";
    private static final Map<String,String> DRIVE_CAPACITY_MEGABYTES_DEFAULT = new HashMap<String,String>();
    
    private StringProperties properties;
    
    /**
     * Default constructor
     */
    public ManualCapacityScaleConfig() {
        this.properties = new StringProperties();
    }
    
    public ManualCapacityScaleConfig(Map<String,String> properties) {
        this.properties = new StringProperties(properties);
    }

    /**
     * Specifies the total memory capacity of the processing unit.
     */
    public void setMemoryCapacityInMB(long memory) {
        properties.putLong(MEMORY_CAPACITY_MEGABYTES_KEY, memory);
    }

    public long getMemoryCapacityInMB() throws NumberFormatException {
        return properties.getLong(MEMORY_CAPACITY_MEGABYTES_KEY, MEMORY_CAPACITY_MEGABYTES_DEFAULT);
    }
    
    /**
     * Specifies the total CPU cores for the processing unit.
     */
    public void setNumberOfCpuCores(double cpuCores) {
        properties.putDouble(CPU_CAPACITY_CORES_KEY, cpuCores);
    }

    public double getNumberOfCpuCores() {
        return properties.getDouble(CPU_CAPACITY_CORES_KEY, CPU_CAPACITY_CORES_DEFAULT);
    }

    public void setPollingIntervalSeconds(int seconds) {
        ScaleStrategyConfigUtils.setPollingIntervalSeconds(properties,seconds);
    }
    
    public int getPollingIntervalSeconds() {
        return ScaleStrategyConfigUtils.getPollingIntervalSeconds(properties);
    }

    public int getMaxConcurrentRelocationsPerMachine() {
        return ScaleStrategyConfigUtils.getMaxConcurrentRelocationsPerMachine(properties);
    }
    
    public void setMaxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        ScaleStrategyConfigUtils.setMaxConcurrentRelocationsPerMachine(properties, maxNumberOfConcurrentRelocationsPerMachine);
    }
        
    public Map<String,String> getProperties() {
        return properties.getProperties();
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    public String getBeanClassName() {
        return ManualCapacityScaleStrategyBean.class.getName();
    }
   
    public String toString() {
        return this.properties.toString();
    }

    /**
     * Specifies the disk and network drive capacity needed by the processing unit.
     * @param megaBytesPerDrive - a mapping between the file system directory representing the drive and its capacity (in mega-bytes) needed by the pu .
     * 
     * For example the drive "/" (on linux) has the size of 50*1024MBs
     * or the drive "c:\" (on windows)  has the size of 50*1024MBs
     */
    public void setDrivesCapacityInMB(Map<String,Long> megaBytesPerDrive) {
        Map<String,String> capacityPerDrive = new HashMap<String,String>();
        for (String drive : megaBytesPerDrive.keySet()) {
            capacityPerDrive.put(drive, megaBytesPerDrive.get(drive).toString());
        }
        properties.putKeyValuePairs(
                DRIVE_CAPACITY_MEGABYTES_KEY, 
                capacityPerDrive, 
                DRIVE_CAPACITY_MEGABYTES_PAIR_SEPERATOR, 
                DRIVE_CAPACITY_MEGABYTES_KEYVALUE_SEPERATOR);
    }
    
    public Map<String,Long> getDrivesCapacityInMB() throws NumberFormatException{
        Map<String, String> capacityPerDrive = properties.getKeyValuePairs(
                DRIVE_CAPACITY_MEGABYTES_KEY, 
                DRIVE_CAPACITY_MEGABYTES_PAIR_SEPERATOR, 
                DRIVE_CAPACITY_MEGABYTES_KEYVALUE_SEPERATOR, 
                DRIVE_CAPACITY_MEGABYTES_DEFAULT);
        
        Map<String,Long> megaBytesPerDrive = new HashMap<String,Long>();
        for (String drive : capacityPerDrive.keySet()) {
            megaBytesPerDrive.put(drive, Long.valueOf(capacityPerDrive.get(drive)));
        }
        return megaBytesPerDrive;
    }
    
    @Override
    public boolean equals(Object other) {
        return (other instanceof ManualCapacityScaleConfig) &&
                this.properties.equals(((ManualCapacityScaleConfig)other).properties);
    }
    
    @Override
    public int hashCode() {
        return this.properties.hashCode();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(this.properties.getProperties());
        
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.properties = new StringProperties((Map<String,String>)in.readObject());
        
    }
    
}
