package org.openspaces.admin.strategy;

public interface StrategyManager<S extends StrategyConfig> {

	<T extends S> void enableStrategy(Class<T> clazz) throws StrategyNotFoundException;

	<T extends S> void disableStrategy(Class<T> clazz) throws StrategyNotFoundException;

	<T extends S> void removeStrategy(Class<T> clazz) throws StrategyNotFoundException;

	void addStrategy(S config)	throws StrategyAlreadyExistsException;

	void setStrategy(S config) throws StrategyNotFoundException;

	<T extends S> T getStrategy(Class<T> clazz) throws StrategyNotFoundException;
}