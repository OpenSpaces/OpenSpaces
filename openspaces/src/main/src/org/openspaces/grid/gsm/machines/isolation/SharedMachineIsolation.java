package org.openspaces.grid.gsm.machines.isolation;

/**
 * Indicates that the processing unit does not require dedicated machines.
 * It can be deployed on machines with other processing units that require shared machine isolation with the same sharingId.
 * It cannot be deployed with public processing units nor with shared processing units that have a different sharingId.
 * 
 * @author itaif
 *
 */
public class SharedMachineIsolation extends ElasticProcessingUnitMachineIsolation {

    private final String sharingId;

    public SharedMachineIsolation(String sharingId) {
        
        if (sharingId == null || sharingId.length() == 0) {
            throw new IllegalArgumentException("sharingId");
        }
        this.sharingId = sharingId;
    }
    
    public String toString() {
        return "shared-machine-isolation-" + sharingId;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SharedMachineIsolation &&
               ((SharedMachineIsolation)other).sharingId.equals(sharingId);
    }
    
    @Override
    public int hashCode() {
        return sharingId.hashCode();
    }
}
