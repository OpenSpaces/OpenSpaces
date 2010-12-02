package org.openspaces.admin.alerts.strategy;

public class MachineCpuUtilizationAlertStrategyConfigurer implements AlertStrategyConfigurer {

	private final MachineCpuUtilizationAlertStrategyConfig alertState = new MachineCpuUtilizationAlertStrategyConfig();
	
	public MachineCpuUtilizationAlertStrategyConfigurer() {
	}
	
	public MachineCpuUtilizationAlertStrategyConfigurer applyRecommendedSettings() {
		lowThresholdPerc(60);
		highThresholdPerc(70);
		movingAveragePeriod(5);
		return this;
	}
	
	public MachineCpuUtilizationAlertStrategyConfigurer highThresholdPerc(int highThreshold) {
		alertState.setHighThresholdPerc(highThreshold);
		return this;
	}
	
	public MachineCpuUtilizationAlertStrategyConfigurer lowThresholdPerc(int lowThreshold) {
		alertState.setLowThresholdPerc(lowThreshold);
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
