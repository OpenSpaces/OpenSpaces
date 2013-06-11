package org.openspaces.grid.gsm.machines.exceptions;

import org.openspaces.admin.internal.machine.events.DefaultElasticMachineProvisioningFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class CloudCleanupFailedException extends
		MachinesSlaEnforcementInProgressException implements SlaEnforcementFailure {

	private static final long serialVersionUID = 1L;

	public CloudCleanupFailedException(ProcessingUnit pu, Exception cause) {
		super(pu, "Failed to cleanup cloud: " +  cause.getMessage(), cause);
	}

    @Override
    public InternalElasticProcessingUnitFailureEvent toEvent() {
        DefaultElasticMachineProvisioningFailureEvent event = new DefaultElasticMachineProvisioningFailureEvent(); 
        event.setFailureDescription(getMessage());
        event.setProcessingUnitName(getProcessingUnitName());
        return event;
    }
}
