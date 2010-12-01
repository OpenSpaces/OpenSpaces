package org.openspaces.admin.alerts.strategy;

import java.util.HashMap;
import java.util.Map;

public class CpuUtilizationAlertStrategyConfig implements AlertStrategyConfig {
	private static final String MOVING_AVERAGE_PERIOD = "movingAveragePeriod";
	private static final String LOW_THRESHOLD = "lowThreshold";
	private static final String HIGH_THRESHOLD = "highThreshold";
	private static final long serialVersionUID = 1L;
	private Map<String, String> properties = new HashMap<String, String>();

	public CpuUtilizationAlertStrategyConfig() {
	}
	
	public CpuUtilizationAlertStrategyConfig setHighThreshold(int highThreshold) {
		properties.put(HIGH_THRESHOLD, String.valueOf(highThreshold));
		return this;
	}
	
	public int getHighThreshold() {
		return Integer.valueOf(properties.get(HIGH_THRESHOLD));
	}
	
	public CpuUtilizationAlertStrategyConfig setLowThreshold(int lowThreshold) {
		properties.put(LOW_THRESHOLD, String.valueOf(lowThreshold));
		return this;
	}
	
	public int getLowThreshold() {
		return Integer.valueOf(properties.get(LOW_THRESHOLD));
	}
	
	public CpuUtilizationAlertStrategyConfig setMovingAveragePeriod(int period) {
		properties.put(MOVING_AVERAGE_PERIOD, String.valueOf(period));
		return this;
	}
	
	public int getMovingAveragePeriod() {
		return Integer.valueOf(properties.get(MOVING_AVERAGE_PERIOD));
	}
	
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public Map<String, String> getProperties() {
		return properties;
	}
	
	public String getStartegyName() {
		return "";//TODO CpuUtilizationAlertStrategyBean.class.getSimpleName();
	}

}
