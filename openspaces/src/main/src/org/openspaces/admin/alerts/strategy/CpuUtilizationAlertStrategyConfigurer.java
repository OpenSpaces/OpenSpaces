package org.openspaces.admin.alerts.strategy;

public class CpuUtilizationAlertStrategyConfigurer implements AlertStrategyConfigurer {

	private final CpuUtilizationAlertStrategyConfig alertState = new CpuUtilizationAlertStrategyConfig();
	
	public CpuUtilizationAlertStrategyConfigurer() {
	}
	
	public CpuUtilizationAlertStrategyConfigurer applyDefaults() {
		lowThreshold(60);
		highThreshold(70);
		movingAveragePeriod(5);
		return this;
	}
	
	public CpuUtilizationAlertStrategyConfigurer highThreshold(int highThreshold) {
		alertState.setHighThreshold(highThreshold);
		return this;
	}
	
	public CpuUtilizationAlertStrategyConfigurer lowThreshold(int lowThreshold) {
		alertState.setLowThreshold(lowThreshold);
		return this;
	}
	
	public CpuUtilizationAlertStrategyConfigurer movingAveragePeriod(int period) {
		alertState.setMovingAveragePeriod(period);
		return this;
	}
	
	public CpuUtilizationAlertStrategyConfig getConfig() {
		return alertState;
	}
}
