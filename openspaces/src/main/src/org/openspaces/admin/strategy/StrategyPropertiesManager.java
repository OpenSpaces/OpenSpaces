package org.openspaces.admin.strategy;

import java.util.Map;

public interface StrategyPropertiesManager {
	
	void addStrategy(String strategy, Map<String,String> properties) throws StrategyAlreadyExistsException;
	
	void setStrategy(String strategy, Map<String,String> properties) throws StrategyNotFoundException;

	void enableStrategy(String strategy) throws StrategyNotFoundException;
	
	void disableStrategy(String strategy) throws StrategyNotFoundException;

	void removeStrategy(String strategy) throws StrategyNotFoundException;
	
	Map<String,String> getStrategy(String strategy) throws StrategyNotFoundException;;
	
	String[] getStrategies();
    
	String[] getEnabledStrategies();

	void disableAllStrategies();
}
