package org.openspaces.grid.gsm.capacity;

public class NumberOfMachinesCapacityRequirement extends AbstractCapacityRequirement {

	public NumberOfMachinesCapacityRequirement() {
		super();
	}
	
	public NumberOfMachinesCapacityRequirement(Long numberOfMachines) {
	    super(numberOfMachines);
	}
	
	public NumberOfMachinesCapacityRequirement(Integer numberOfMachines) {
		super(numberOfMachines);
	}
	
	public int getNumberOfMachines() {
		return Integer.valueOf(value.toString());
	}

    @SuppressWarnings("unchecked")
    public CapacityRequirementType<NumberOfMachinesCapacityRequirement> getType() {
	    return (CapacityRequirementType<NumberOfMachinesCapacityRequirement>) super.getType();
	}
	
    @Override
    public String toString() {
        return getNumberOfMachines() +" machines";
    }
}
