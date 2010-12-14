package org.openspaces.grid.gsm.capacity;

import org.openspaces.admin.AdminException;


public class CapacityRequirements {

	private final CapacityRequirement[] requirements;
	
	public CapacityRequirements(CapacityRequirement... requirements) {
		this.requirements = requirements;
	}
	
	public CapacityRequirement[] getRequirements() {
		return this.requirements;
	}
	
	public <T extends CapacityRequirement> T getRequirement(Class<T> type) {
		T requirement = null;
		
		for (CapacityRequirement r : requirements) {
			if (type.isInstance(r)) {
				requirement = type.cast(r);
				break;
			}
		}
		
		if (requirement == null) {
			try {
				requirement = type.newInstance();
			} catch (InstantiationException e) {
				throw new AdminException("Cannot construct an empty " + type.getName(),e);
			} catch (IllegalAccessException e) {
				throw new AdminException("Cannot construct an empty " + type.getName(),e);
			}
		}
		
		return requirement;
	}
	
}
