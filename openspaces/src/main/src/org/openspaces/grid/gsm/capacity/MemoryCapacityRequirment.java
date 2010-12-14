package org.openspaces.grid.gsm.capacity;

import org.openspaces.core.util.MemoryUnit;

public class MemoryCapacityRequirment implements CapacityRequirement{

	private final long memoryInMB;
	
	public MemoryCapacityRequirment() {
		this(0);
	}
	
	public MemoryCapacityRequirment(long memoryInMB) {
		this.memoryInMB = memoryInMB;
	}

	public MemoryCapacityRequirment(String memory) {
		this(MemoryUnit.MEGABYTES.convert(memory));
	}
	
	public MemoryCapacityRequirment(long memory, MemoryUnit unit) {
		this(unit.toMegaBytes(memory));
	}
	
	public long getMemoryInMB() {
		return this.memoryInMB;
	}
	
}
