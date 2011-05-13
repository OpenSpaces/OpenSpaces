package org.openspaces.grid.gsm.capacity;

/**
 * A machine capacity requirement for the {@link org.openspaces.grid.gsm.machines.plugins.ElasticMachineProvisioning}
 * 
 * Each implementation must have a public default constructor that creates a zero capacity requirement object.
 * Each implementation must be immutable.
 * 
 * @author itaif
 * @see CapacityRequirements
 */
public interface CapacityRequirement extends Comparable<CapacityRequirement> {

    String toString();
    
    boolean equals(Object otherCapacityRequirement);

    boolean equalsZero();
    
    CapacityRequirement multiply(int i);
    
    CapacityRequirement divide(int numberOfContainers);
    
    CapacityRequirement subtract(CapacityRequirement otherCapacityRequirement);
    
    CapacityRequirement subtractOrZero(CapacityRequirement otherCapacityRequirement);
    
    CapacityRequirement add(CapacityRequirement otherCapacityRequirement);

    CapacityRequirement min(CapacityRequirement otherCapacityRequirement);

    CapacityRequirement max(CapacityRequirement otherCapacityRequirement);

    double divide(CapacityRequirement otherCapacityRequirement);

    CapacityRequirementType<? extends CapacityRequirement> getType();
}
