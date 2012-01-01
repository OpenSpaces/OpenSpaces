package org.openspaces.grid.gsm.machines.exceptions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.machines.AbstractMachinesSlaPolicy;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementState;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class NeedToStartMoreGridServiceAgentsException extends GridServiceAgentSlaEnforcementInProgressException implements SlaEnforcementFailure {
    
    private static final long serialVersionUID = 1L;
    private final String[] affectedProcessingUnits;

    public NeedToStartMoreGridServiceAgentsException(AbstractMachinesSlaPolicy sla, MachinesSlaEnforcementState state, CapacityRequirements capacityShortage, ProcessingUnit pu) {
        super(createMessage(sla, state, capacityShortage, pu));
        this.affectedProcessingUnits  = new String[] {pu.getName()};
    }

    public NeedToStartMoreGridServiceAgentsException(CapacityRequirements capacityShortage, ProcessingUnit pu) {
        super(createBasicMessage(capacityShortage));
        this.affectedProcessingUnits  = new String[] {pu.getName()};
    }

    private static String createBasicMessage(CapacityRequirements capacityShortage) {
        return "Cannot enforce Machines SLA since there are not enough machines available. "+
                "Need more capacity: " + capacityShortage;
    }
    
    private static String createMessage(AbstractMachinesSlaPolicy sla, MachinesSlaEnforcementState state, CapacityRequirements capacityShortage, ProcessingUnit pu) {
        return createBasicMessage(capacityShortage) + " " + reportToString(sla, createReportOfAllMachines(state, pu));
    }

    private static String reportToString(
            AbstractMachinesSlaPolicy sla, Map<GridServiceAgent, Map<ProcessingUnit, CapacityRequirements>> createReportOfAllMachines) {
        StringBuilder message = new StringBuilder("Capacity Report of all relevant machines:");
        for (Entry<GridServiceAgent, Map<ProcessingUnit, CapacityRequirements>> agentpair : createReportOfAllMachines.entrySet()) {
            // add to the report the amount of reserved capacity per machine
            //"reserved-capacity", sla.getReservedCapacityPerMachine());
            GridServiceAgent agent = agentpair.getKey();
            String ipAddress = MachinesSlaUtils.machineToString(agent.getMachine());
            message
                .append("\"").append(ipAddress).append("\":{")
                .append("reserved:").append(sla.getReservedCapacityPerMachine()).append(",");
            for (Entry<ProcessingUnit,CapacityRequirements> pupair : agentpair.getValue().entrySet()) {
                String puName = pupair.getKey().getName();
                String capacity = pupair.getValue().toString();
                message.append(puName).append(":").append(capacity).append(",");
            }
            int lastIndex = message.length()-1;
            if (message.charAt(lastIndex)==',') {
                message.deleteCharAt(lastIndex);
            }
            message.append("},");
        }
        int lastIndex = message.length()-1;
        if (message.charAt(lastIndex)==',') {
            message.deleteCharAt(lastIndex);
        }
        return message.toString();
    }

    private static Map<GridServiceAgent, Map<ProcessingUnit, CapacityRequirements>> createReportOfAllMachines(MachinesSlaEnforcementState state, ProcessingUnit pu) {
        
        // create a report for each relevant agent - which pus are installed on it and how much capacity they are using
        Map<GridServiceAgent,Map<ProcessingUnit,CapacityRequirements>> capacityPerProcessingUnitPerAgentUid = new HashMap<GridServiceAgent,Map<ProcessingUnit,CapacityRequirements>>();
        Collection<String> restrictedAgentUids = state.getRestrictedAgentUidsForPu(pu);
        Map<ProcessingUnit, ClusterCapacityRequirements> allocatedCapacityPerProcessingUnit = state.getAllocatedCapacityPerProcessingUnit();
        for (Entry<ProcessingUnit, ClusterCapacityRequirements> pair : allocatedCapacityPerProcessingUnit.entrySet()) {
            ProcessingUnit otherPu = pair.getKey();
            for (String agentUid : pair.getValue().getAgentUids()) {
                if (!restrictedAgentUids.contains(agentUid)) {
                    GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);
                    if (agent != null) {
                        CapacityRequirements otherPUCapacityOnAgent = pair.getValue().getAgentCapacityOrZero(agentUid);
                        if (!capacityPerProcessingUnitPerAgentUid.containsKey(agent)) {
                            capacityPerProcessingUnitPerAgentUid.put(agent, new HashMap<ProcessingUnit, CapacityRequirements>());
                        }
                        capacityPerProcessingUnitPerAgentUid.get(agent).put(otherPu, otherPUCapacityOnAgent);
                    }
                }
            }
        }
        return capacityPerProcessingUnitPerAgentUid;
    }

    @Override
    public String[] getAffectedProcessingUnits() {
        return affectedProcessingUnits;
    }
}