package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;

public class MachinesSlaUtils {

    public static List<GridServiceAgent> sortGridServiceAgentsByManagementComponentsLast(GridServiceAgent[] agents) {
        List<GridServiceAgent> sortedAgents = new ArrayList<GridServiceAgent>(Arrays.asList(agents));
        Collections.sort(sortedAgents,new Comparator<GridServiceAgent>() {

            public int compare(GridServiceAgent agent1, GridServiceAgent agent2) {
                int numberOfManagementComponents1 = getNumberOfChildProcesses(agent1) - getNumberOfChildContainers(agent1);
                int numberOfManagementComponents2 = getNumberOfChildProcesses(agent2) - getNumberOfChildContainers(agent2);
                return numberOfManagementComponents1 - numberOfManagementComponents2;
            }
        });
        return sortedAgents;
    }
    
    public static int getNumberOfChildProcesses(final GridServiceAgent agent) {
        int numberOfChildProcesses = agent.getProcessesDetails().getProcessDetails().length;
        return numberOfChildProcesses;
    }

    public static int getNumberOfChildContainers(final GridServiceAgent agent) {
        int numberOfContainers = 0;
        for (final GridServiceContainer container : agent.getAdmin().getGridServiceContainers()) {
            if (container.getGridServiceAgent() != null && container.getGridServiceAgent().equals(agent)) {
                numberOfContainers++;
            }
        }
        return numberOfContainers;
    }


}
