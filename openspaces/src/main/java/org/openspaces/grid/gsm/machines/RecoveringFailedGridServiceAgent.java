package org.openspaces.grid.gsm.machines;

public class RecoveringFailedGridServiceAgent {

	private final Object agentContext;
	private final String agentUid;
	private int recoveryAttempts;
	
	/**
	 * @param agentUid - the agent UID
	 * @param agentContext - the agent context as provided by the machine provisioning (cloud driver).
	 * recoveryAttempts is zero.
	 */
	public RecoveringFailedGridServiceAgent(String agentUid, Object agentContext) {
		this.agentUid = agentUid;
		this.agentContext = agentContext;
		this.recoveryAttempts = 0;
	}

	/**
	 * @return 0 if this is a new machine, 1 if the first recovery attempt of a failed machine, 2 if the second recovery attempt, etc...
	 */
	public int getRecoveryAttempts() {
		return recoveryAttempts;
	}
	
	/**
	 * Increments the number of recovery attempts
	 * Call before each time a machine is started as an attempt to recover from failure.
	 */
	public void incrementRecoveryAttempt() {
		recoveryAttempts++;
	}
	
	public String getAgentUid() {
		return agentUid;
	}

	public Object getAgentContext() {
		return agentContext;
	}
	
	public FailedGridServiceAgent toFailedGridServiceAgent() {
		return new FailedGridServiceAgent(agentUid, agentContext, recoveryAttempts);
	}
}
