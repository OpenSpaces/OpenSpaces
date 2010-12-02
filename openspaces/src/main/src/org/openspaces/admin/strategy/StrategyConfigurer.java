package org.openspaces.admin.strategy;

public interface StrategyConfigurer<T extends StrategyConfig> {
    StrategyConfigurer<T> applyRecommendedSettings();
	T getConfig();
}
