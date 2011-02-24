package org.openspaces.grid.gsm.machines.isolation;

/**
 * Indicates that the processing unit requires dedicated machines.
 * It cannot be deployed on machines with other processing units.
 * 
 * @author itaif
 *
 */
public class DedicatedMachineIsolation extends ElasticProcessingUnitMachineIsolation {

    private final String puName;
    
    public DedicatedMachineIsolation(String puName) {
        if (puName == null || puName.length() == 0) {
            throw new IllegalArgumentException("puName");
        }
        this.puName = puName;
    }
    
    public String toString() {
        return "dedicated-isolation-"+puName;
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof DedicatedMachineIsolation &&
               ((DedicatedMachineIsolation)other).puName.equals(this.puName);
    }
    
    @Override
    public int hashCode() {
         return puName.hashCode();
    }
}
