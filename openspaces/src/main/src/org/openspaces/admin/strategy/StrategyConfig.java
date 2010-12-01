package org.openspaces.admin.strategy;

import java.util.Map;

public interface StrategyConfig {
	
	String getStartegyName();
	
	void setProperties(Map<String,String> properties);
	Map<String,String> getProperties();
}
