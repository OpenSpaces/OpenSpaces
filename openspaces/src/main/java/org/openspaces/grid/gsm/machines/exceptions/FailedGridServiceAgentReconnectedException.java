package org.openspaces.grid.gsm.machines.exceptions;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.gsa.events.DefaultElasticGridServiceAgentProvisioningFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.machines.FailedGridServiceAgent;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

/**
 * An exception that is caused when an agent considered to be failed, is actually discovered.
 * This is a problem when a new machine was started to replace that failed agent.
 * 
 * @author itaif
 * @since 9.7
 */
public class FailedGridServiceAgentReconnectedException extends GridServiceAgentSlaEnforcementInProgressException implements SlaEnforcementFailure {
		    
	    private static final long serialVersionUID = 1L;
		
	    private final String newAgentUid;
		private final String failedAgentUid;
		private final int failedAgentRecoveryAttempt;

		private final GridServiceAgent newAgent;
		private final GridServiceAgent reconnectedFailedAgent;
	    
	    public FailedGridServiceAgentReconnectedException(ProcessingUnit pu, GridServiceAgent newAgent, FailedGridServiceAgent failedAgent, GridServiceAgent reconnectedFailedAgent) {
	        super(pu, "New agent " + MachinesSlaUtils.agentToString(newAgent) +
	                " was started, in response to a machine failure, but the failed agent it was meant to replace has reconnected: " + MachinesSlaUtils.agentToString(reconnectedFailedAgent));
	        this.newAgentUid = newAgent.getUid();
	        this.failedAgentUid = reconnectedFailedAgent.getUid();
	        this.failedAgentRecoveryAttempt = failedAgent.getRecoveryAttempts();
	        
	        this.newAgent = newAgent;
	        this.reconnectedFailedAgent = reconnectedFailedAgent;
	    }

	    
	    @Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + failedAgentRecoveryAttempt;
			result = prime
					* result
					+ ((failedAgentUid == null) ? 0 : failedAgentUid
							.hashCode());
			result = prime * result
					+ ((newAgentUid == null) ? 0 : newAgentUid.hashCode());
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			FailedGridServiceAgentReconnectedException other = (FailedGridServiceAgentReconnectedException) obj;
			if (failedAgentRecoveryAttempt != other.failedAgentRecoveryAttempt)
				return false;
			if (failedAgentUid == null) {
				if (other.failedAgentUid != null)
					return false;
			} else if (!failedAgentUid.equals(other.failedAgentUid))
				return false;
			if (newAgentUid == null) {
				if (other.newAgentUid != null)
					return false;
			} else if (!newAgentUid.equals(other.newAgentUid))
				return false;
			return true;
		}


		@Override
	    public InternalElasticProcessingUnitFailureEvent toEvent() {
	        DefaultElasticGridServiceAgentProvisioningFailureEvent event = new DefaultElasticGridServiceAgentProvisioningFailureEvent(); 
	        event.setFailureDescription(getMessage());
	        event.setProcessingUnitName(getProcessingUnitName());
	        return event;
	    }

		public GridServiceAgent getNewAgent() {
			return newAgent;
		}
		
		public GridServiceAgent getOldAgent() {
			return reconnectedFailedAgent;
		}
}
