package org.openspaces.grid.gsm.capacity;

public class CpuCapacityRequirement implements CapacityRequirement {

	private final double cpu;

	public CpuCapacityRequirement() {
		this(0.0);
	}
	
	public CpuCapacityRequirement(double cpu) {
		this.cpu = cpu;
	}

	public double getCpu() {
		return this.cpu;
	}
}
