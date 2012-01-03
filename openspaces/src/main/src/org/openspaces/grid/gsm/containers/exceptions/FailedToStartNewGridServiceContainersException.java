package org.openspaces.grid.gsm.containers.exceptions;

import java.util.Arrays;

import org.openspaces.admin.machine.Machine;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;


public class FailedToStartNewGridServiceContainersException extends ContainersSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    private final String[] affectedProcessingUnits;
    private final String machineUid;
    
    public FailedToStartNewGridServiceContainersException(Machine machine, String[] affectedProcessingUnits, Exception reason) {
        super(createMessage(machine, reason),reason);
        this.machineUid = machine.getUid();
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
    
    @Override
    public boolean equals(Object other) {
        boolean same = false;
        if (other instanceof FailedToStartNewGridServiceContainersException) {
            FailedToStartNewGridServiceContainersException otherEx = (FailedToStartNewGridServiceContainersException)other;
            same = Arrays.equals(otherEx.affectedProcessingUnits,this.affectedProcessingUnits) && 
                    otherEx.getCause().getMessage().equals(getCause().getMessage()) &&
                    otherEx.machineUid.equals(machineUid);
        }
        return same;  
    }
}
