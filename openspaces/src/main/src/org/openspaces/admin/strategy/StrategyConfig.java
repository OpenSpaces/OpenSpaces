package org.openspaces.admin.strategy;

import java.util.Map;

public interface StrategyConfig {
	
	String getStartegyBeanClassName();
	
	void applyRecommendedSettings();
	void setProperties(Map<String,String> properties);
	Map<String,String> getProperties();
}
