package org.openspaces.grid.gsm.capacity;

public class NumberOfMachinesCapacityRequirement implements CapacityRequirement {

	private final int numberOfMachines;
	
	public NumberOfMachinesCapacityRequirement() {
		this(0);
	}
	
	public NumberOfMachinesCapacityRequirement(int numberOfMachines) {
		this.numberOfMachines = numberOfMachines;
	}
	
	public int getNumberOfMahines() {
		return numberOfMachines;
	}
}
