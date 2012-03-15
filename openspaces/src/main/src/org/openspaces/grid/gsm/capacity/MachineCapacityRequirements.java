/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.grid.gsm.capacity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.os.OperatingSystemDetails.DriveDetails;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.core.util.MemoryUnit;

public class MachineCapacityRequirements extends CapacityRequirements {

    Machine machine;
    
    public MachineCapacityRequirements(Machine machine) {
        super(getMachineRequirements(machine));
        this.machine = machine;
    }
    
    public static CapacityRequirement[] getMachineRequirements(Machine machine) {
        List<CapacityRequirement> requirements = new ArrayList<CapacityRequirement>();
        requirements.add(getCpu(machine));
        requirements.add(getMemory(machine));
        requirements.addAll(getDrives(machine));
        return requirements.toArray(new CapacityRequirement[requirements.size()]);
        
    }
    
    private static MemoryCapacityRequirement getMemory(Machine machine) {
        
        OperatingSystemDetails osDetails = machine.getOperatingSystem().getDetails();
        long memoryInMB = MemoryUnit.BYTES.toMegaBytes(
                    osDetails.getTotalPhysicalMemorySizeInBytes());
        if (memoryInMB < 0 ) {
            memoryInMB = 0;
        }
        return new MemoryCapacityRequirement(memoryInMB);
    }
    
    private static List<DriveCapacityRequirement> getDrives(Machine machine) {
        List<DriveCapacityRequirement> drives = new ArrayList<DriveCapacityRequirement>();
        Map<String,DriveDetails> osDetails = machine.getOperatingSystem().getDetails().getDriveDetails();
        for (String drive : osDetails.keySet()) {
            drives.add( 
                    new DriveCapacityRequirement(drive,
                    osDetails.get(drive).getCapacityInMB()));
        }
        return drives;
    }
    
    private static CpuCapacityRequirement getCpu(Machine machine) {
        int availableProcessors = machine.getOperatingSystem().getDetails().getAvailableProcessors();
        return new CpuCapacityRequirement(new Fraction(availableProcessors,1));
        
    }
}
