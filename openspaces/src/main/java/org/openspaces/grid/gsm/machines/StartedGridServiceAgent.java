package org.openspaces.grid.gsm.machines;

import org.openspaces.admin.gsa.GridServiceAgent;

public class StartedGridServiceAgent {

    private GridServiceAgent agent;
    private Object context;

    public StartedGridServiceAgent(GridServiceAgent agent, Object context) {
    	this.agent = agent;
    	this.context = context;
    }

    public GridServiceAgent getAgent() {
    	return this.agent;
    }

    public Object getAgentContext() {
    	return this.context;
    }

    @Override
    public String toString() {
        return "StartedGridServiceAgent " + MachinesSlaUtils.agentToString(agent);
    }
}
