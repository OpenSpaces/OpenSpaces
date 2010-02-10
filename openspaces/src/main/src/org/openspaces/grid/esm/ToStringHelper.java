package org.openspaces.grid.esm;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

public class ToStringHelper {
    
    public static String puToString(ProcessingUnit pu) {
        StringBuilder sb = new StringBuilder();
        sb.append("Processing Unit Configuration:")
        .append("\n\tName: ").append(pu.getName())
        .append("\n\tInstances: ").append(pu.getNumberOfInstances())
        .append("\n\tBackups: ").append(pu.getNumberOfBackups())
        .append("\n\tInstances per-machine: ").append(pu.getMaxInstancesPerMachine())
        .append("\n\tPlanned instances: ").append(pu.getTotalNumberOfInstances())
        .append("\n\tActual instances: ").append(pu.getProcessingUnitInstances().length)
        .append("\n\tDeployment status: ").append(pu.getStatus())
        ;
        return sb.toString();
    }

    public static String puCapacityPlannerToString(PuCapacityPlanner puCapacityPlanner) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\t status: ").append(puCapacityPlanner.getProcessingUnit().getStatus())
        .append("\n\t minNumberOfGSCs=").append(puCapacityPlanner.getMinNumberOfGSCs())
        .append("\n\t maxNumberOfGSCs=").append(puCapacityPlanner.getMaxNumberOfGSCs())
        .append("\n\t actual number of GSCs: ").append(puCapacityPlanner.getProcessingUnit().getAdmin().getGridServiceContainers().getSize())
        .append("\n\t maxNumberOfGSCsPerMachine=").append(puCapacityPlanner.getMaxNumberOfGSCsPerMachine()).append( " (initial)")
        .append("\n\t scalingFactor=").append(+puCapacityPlanner.getScalingFactor()).append(" (instances per GSC)")
        .append("\n\t number of GSCs in zone: ").append(puCapacityPlanner.getZoneName()).append("=").append(puCapacityPlanner.getNumberOfGSCsInZone());
        
        return sb.toString();
    }
    
    public static String machineToString(Machine machine) {
        return machine.getHostName() + "/" + machine.getHostAddress();
    }
    
    public static String gscToString(GridServiceContainer container) {
        return "pid["+container.getVirtualMachine().getDetails().getPid()+"] host["+machineToString(container.getMachine())+"]";
    }
    
    public static String puInstanceToString(ProcessingUnitInstance instance) {
        String backupId = "";
        if (instance.getProcessingUnit().getNumberOfBackups() > 0) {
            backupId = instance.getBackupId() == 0 ? "[1]" : "[2]";
        }
        return instance.getInstanceId() + " " + backupId;
    }
}
