package org.openspaces.grid.gsm.machines.isolation;

public abstract class ElasticProcessingUnitMachineIsolation {
    
    /**
     * @return true if this processing unit can be deployed on the same machine as the specified processing unit deployment. 
     */
    public abstract boolean equals(Object otherProcessingUnitIsolation);
}
