package org.openspaces.admin.strategy;

public interface StrategyConfigurer<T extends StrategyConfig> {
	T getConfig();
}
