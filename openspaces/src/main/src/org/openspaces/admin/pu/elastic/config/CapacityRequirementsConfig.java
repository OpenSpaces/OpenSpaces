/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.admin.pu.elastic.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.capacity.CapacityRequirement;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.DriveCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;

/**
 * A config object for configuring capacity requirements
 * @author itaif
 * @since 9.0.0
 */
public class CapacityRequirementsConfig implements ScaleStrategyCapacityRequirementConfig {

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
    public CapacityRequirementsConfig() {
        this.properties = new StringProperties();
    }
    
    public CapacityRequirementsConfig(Map<String,String> properties) {
        this.properties = new StringProperties(properties);
    }
    
    /**
     * @param newCapacity
     */
    public CapacityRequirementsConfig(CapacityRequirements newCapacity) {
        this();
        long memoryInMB = 0;
        Map<String, Long> megaBytesPerDrive = new HashMap<String, Long>();
        double cpuCores = 0;
        for (CapacityRequirement capacityRequirement : newCapacity.getRequirements()) {
            if (capacityRequirement instanceof MemoryCapacityRequirement) {
                MemoryCapacityRequirement memoryCapacityRequirement = (MemoryCapacityRequirement) capacityRequirement;
                memoryInMB +=memoryCapacityRequirement.getMemoryInMB(); 
            }
            else if (capacityRequirement instanceof DriveCapacityRequirement) {
                DriveCapacityRequirement driveCapacityRequirement = (DriveCapacityRequirement) capacityRequirement;
                megaBytesPerDrive.put(driveCapacityRequirement.getDrive(), driveCapacityRequirement.getDriveCapacityInMB());
            }
            else if (capacityRequirement instanceof CpuCapacityRequirement) {
                CpuCapacityRequirement cpuCapacityRequirement = (CpuCapacityRequirement) capacityRequirement;
                cpuCores += cpuCapacityRequirement.getCpu().doubleValue();
            }
            else {
                throw new IllegalArgumentException("Cannot convert " + capacityRequirement.getClass() + "(" + capacityRequirement + ") into " + this.getClass());
            }
        }
        
        if (memoryInMB > 0) {
            setMemoryCapacityInMB(memoryInMB);
        }
        
        if (!megaBytesPerDrive.isEmpty()) {
            setDrivesCapacityInMB(megaBytesPerDrive);
        }
        
        if (cpuCores > 0) {
            setNumberOfCpuCores(cpuCores);
        }
    }

    @Override
    public void setMemoryCapacityInMB(long memory) {
        properties.putLong(MEMORY_CAPACITY_MEGABYTES_KEY, memory);
    }

    @Override
    public long getMemoryCapacityInMB() throws NumberFormatException {
        return properties.getLong(MEMORY_CAPACITY_MEGABYTES_KEY, MEMORY_CAPACITY_MEGABYTES_DEFAULT);
    }
    
    @Override
    public void setNumberOfCpuCores(double cpuCores) {
        properties.putDouble(CPU_CAPACITY_CORES_KEY, cpuCores);
    }

    @Override
    public double getNumberOfCpuCores() {
        return properties.getDouble(CPU_CAPACITY_CORES_KEY, CPU_CAPACITY_CORES_DEFAULT);
    }

    public Map<String,String> getProperties() {
        return properties.getProperties();
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }
    
    @Override
    public String toString() {
        return this.properties.toString();
    }

    @Override
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
    
    @Override
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CapacityRequirementsConfig other = (CapacityRequirementsConfig) obj;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        return true;
    }

    @Override
    public CapacityRequirements toCapacityRequirements() {
       
        CapacityRequirements capacityRequirements = 
          new CapacityRequirements()
          .add(new MemoryCapacityRequirement(this.getMemoryCapacityInMB()))
          .add(new CpuCapacityRequirement(this.getNumberOfCpuCores()));
        
        for (Entry<String, Long> pair : this.getDrivesCapacityInMB().entrySet()) {
            capacityRequirements =capacityRequirements.add(
                    new DriveCapacityRequirement(pair.getKey(), pair.getValue()));
        }
        
        return capacityRequirements;
    }
}
