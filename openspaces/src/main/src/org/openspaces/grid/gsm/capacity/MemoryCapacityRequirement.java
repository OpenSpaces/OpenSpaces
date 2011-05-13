package org.openspaces.grid.gsm.capacity;


public class MemoryCapacityRequirement extends AbstractCapacityRequirement {

    public MemoryCapacityRequirement() {
        super();
    }
    
	public MemoryCapacityRequirement(Long memoryInMB) {
        super(memoryInMB);
    }

	public CapacityRequirementType<MemoryCapacityRequirement> getType() {
	    return new CapacityRequirementType<MemoryCapacityRequirement>(MemoryCapacityRequirement.class);
	}
	
    public String toString() {
	    return getMemoryInMB() +"MB RAM";
	}
	
	public long getMemoryInMB() {
        return value;
    }
	
	
}
