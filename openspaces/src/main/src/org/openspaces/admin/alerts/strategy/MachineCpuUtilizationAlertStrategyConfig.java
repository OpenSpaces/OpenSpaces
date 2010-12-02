package org.openspaces.admin.alerts.strategy;

import java.util.Map;

import org.openspaces.admin.internal.alerts.strategy.MachineCpuUtilizationAlertStrategyBean;
import org.openspaces.core.util.StringProperties;

public class MachineCpuUtilizationAlertStrategyConfig implements AlertStrategyConfig {
	private static final String MOVING_AVERAGE_PERIOD = "movingAveragePeriod";
	private static final String LOW_THRESHOLD = "lowThreshold";
	private static final String HIGH_THRESHOLD = "highThreshold";
	private static final long serialVersionUID = 1L;
	private StringProperties properties = new StringProperties();

	public MachineCpuUtilizationAlertStrategyConfig() {
	}
	
	public MachineCpuUtilizationAlertStrategyConfig setHighThreshold(int highThreshold) {
	    properties.putInteger(HIGH_THRESHOLD, highThreshold);
		return this;
	}
	
	public int getHighThreshold() {
	    return properties.getInteger(HIGH_THRESHOLD, Integer.MAX_VALUE);
	}
	
	public MachineCpuUtilizationAlertStrategyConfig setLowThreshold(int lowThreshold) {
	    properties.putInteger(LOW_THRESHOLD, lowThreshold);
		return this;
	}
	
	public int getLowThreshold() {
	    return properties.getInteger(LOW_THRESHOLD, 0);
	}
	
	public MachineCpuUtilizationAlertStrategyConfig setMovingAveragePeriod(int period) {
	    properties.putInteger(MOVING_AVERAGE_PERIOD, period);
		return this;
	}
	
	public int getMovingAveragePeriod() {
	    return properties.getInteger(MOVING_AVERAGE_PERIOD, 60); //5 minute sliding window
	}
	
	public void applyRecommendedSettings() {
	    setLowThreshold(60);
        setHighThreshold(80);
        setMovingAveragePeriod(10);
	}
	
	public void setProperties(Map<String, String> properties) {
	    this.properties = new StringProperties(properties);
	}

	public Map<String, String> getProperties() {
		return properties.getProperties();
	}
	
	public String getStartegyBeanClassName() {
		return MachineCpuUtilizationAlertStrategyBean.class.getSimpleName();
	}

}
