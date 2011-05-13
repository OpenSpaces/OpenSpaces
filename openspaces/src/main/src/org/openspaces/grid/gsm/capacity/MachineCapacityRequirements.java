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
    
    @Override
    public String toString() {
        return "Machine " + machine.getHostAddress() + ":" + super.toString();
    }
}
