package org.openspaces.grid.gsm.containers.exceptions;

import org.openspaces.admin.machine.Machine;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class FailedToStartNewGridServiceContainersException extends ContainersSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    private final String[] affectedProcessingUnits;
    
    public FailedToStartNewGridServiceContainersException(Machine machine, String[] affectedProcessingUnits, Exception reason) {
        super(createMessage(machine, reason),reason);
        this.affectedProcessingUnits = affectedProcessingUnits;
    }

    private static String createMessage(Machine machine, Exception reason) {
        return "Failed to start container on machine "
                + ContainersSlaUtils.machineToString(machine)+ ". "+
                "Caused By:" + reason.getMessage();
    }

    @Override
    public String[] getAffectedProcessingUnits() {
        return affectedProcessingUnits;
    }
}
