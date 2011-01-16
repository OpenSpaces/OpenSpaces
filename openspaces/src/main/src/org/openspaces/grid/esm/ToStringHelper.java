package org.openspaces.grid.esm;

import java.util.List;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.SpaceInstance;

public class ToStringHelper {
    
    public static String puToString(ProcessingUnit pu) {
        StringBuilder sb = new StringBuilder();
        sb.append("Processing Unit Configuration:")
        .append("\n\t Name: ").append(pu.getName())
        .append("\n\t Instances: ").append(pu.getNumberOfInstances())
        .append("\n\t Backups: ").append(pu.getNumberOfBackups())
        .append("\n\t Instances per-machine: ").append(pu.getMaxInstancesPerMachine())
        .append("\n\t Planned instances: ").append(pu.getTotalNumberOfInstances())
        .append("\n\t Actual instances: ").append(pu.getInstances().length)
        .append("\n\t Deployment status: ").append(pu.getStatus())
        .append("\n\t Managed by ESM: ").append(PuCapacityPlanner.isElastic(pu))
        ;
        return sb.toString();
    }

    public static String puCapacityPlannerToString(PuCapacityPlanner puCapacityPlanner) {
        StringBuilder sb = new StringBuilder();
        sb.append(puToString(puCapacityPlanner.getProcessingUnit()))
        .append("\n\t Status: ").append(puCapacityPlanner.getProcessingUnit().getStatus())
        .append("\n\t Min #GSCs: ").append(puCapacityPlanner.getMinNumberOfGSCs())
        .append("\n\t Max #GSCs: ").append(puCapacityPlanner.getMaxNumberOfGSCs())
        .append("\n\t Actual #GSCs: ").append(puCapacityPlanner.getProcessingUnit().getAdmin().getGridServiceContainers().getSize())
        .append("\n\t Instances Per GSC: ").append(+puCapacityPlanner.getScalingFactor())
        .append("\n\t GSCs in zone: ").append(puCapacityPlanner.getContextProperties().getZoneName()).append("=").append(puCapacityPlanner.getNumberOfGSCsInZone());
        
        return sb.toString();
    }
    
    public static String machinesToString(List<Machine> machines, String delim) {
        StringBuilder builder = new StringBuilder();
        for (Machine machine : machines) {
            builder.append(machineToString(machine)).append(delim);
        }
        return builder.toString();
    }
    
    public static String machineToString(Machine machine) {
        return machine.getHostName() + "/" + machine.getHostAddress();
    }
    
    public static String gscToString(GridComponent container) {
        return "pid["+container.getVirtualMachine().getDetails().getPid()+"] host["+machineToString(container.getMachine())+"]";
    }
    
    public static String puInstanceToString(ProcessingUnitInstance instance) {
        StringBuilder builder = new StringBuilder(16);
        builder.append("[").append(instance.getInstanceId()).append(",").append(instance.getBackupId() + 1);
        SpaceInstance spaceInstance = instance.getSpaceInstance();
        if (spaceInstance != null) {
            builder.append(",").append(spaceInstance.getMode());
        }
        builder.append("]");
        return builder.toString();
    }
}
