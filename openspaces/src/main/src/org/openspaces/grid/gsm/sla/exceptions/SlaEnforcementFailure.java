package org.openspaces.grid.gsm.sla.exceptions;

public interface SlaEnforcementFailure {

    public String[] getAffectedProcessingUnits();
    
    /**
     * Must implement the equals method since it is used to filter failure events
     */
    public boolean equals(Object other);

}
