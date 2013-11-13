package org.openspaces.grid.gsm.machines;

public class FailedGridServiceAgent {

	private final Object agentContext;
	private final String agentUid;
	private final int recoveryAttempts;
	
	/**
	 * @param agentUid - the agent UID
	 * @param agentContext - the agent context as provided by the machine provisioning (cloud driver).
	 * recoveryAttempts is zero.
	 * @param recoveryAttempts - 0 if this is a new machine, 1 if the first recovery attempt of a failed machine, 2 if the second recovery attempt, etc...
	 */
	public FailedGridServiceAgent(String agentUid, Object agentContext, int recoveryAttempts) {
		this.agentUid = agentUid;
		this.agentContext = agentContext;
		this.recoveryAttempts = recoveryAttempts;
	}

	/**
	 * @return 0 if this is a new machine, 1 if the first recovery attempt of a failed machine, 2 if the second recovery attempt, etc...
	 */
	public int getRecoveryAttempts() {
		return recoveryAttempts;
	}
	
	public String getAgentUid() {
		return agentUid;
	}

	public Object getAgentContext() {
		return agentContext;
	}
	
	@Override
	public String toString() {
		return "FailedGridServiceAgent [agentUid=" + agentUid + ", recoveryAttempts="
				+ recoveryAttempts + "]";
	}
}
