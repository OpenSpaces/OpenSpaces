package org.openspaces.admin.alerts.strategy;

public class MachineCpuUtilizationAlertStrategyConfigurer implements AlertStrategyConfigurer {

	private final MachineCpuUtilizationAlertStrategyConfig alertState = new MachineCpuUtilizationAlertStrategyConfig();
	
	public MachineCpuUtilizationAlertStrategyConfigurer() {
	}
	
	public MachineCpuUtilizationAlertStrategyConfigurer applyRecommendedSettings() {
		lowThreshold(60);
		highThreshold(70);
		movingAveragePeriod(5);
		return this;
	}
	
	public MachineCpuUtilizationAlertStrategyConfigurer highThreshold(int highThreshold) {
		alertState.setHighThreshold(highThreshold);
		return this;
	}
	
	public MachineCpuUtilizationAlertStrategyConfigurer lowThreshold(int lowThreshold) {
		alertState.setLowThreshold(lowThreshold);
		return this;
	}
	
	public MachineCpuUtilizationAlertStrategyConfigurer movingAveragePeriod(int period) {
		alertState.setMovingAveragePeriod(period);
		return this;
	}
	
	public MachineCpuUtilizationAlertStrategyConfig getConfig() {
		return alertState;
	}
}
