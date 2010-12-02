package org.openspaces.admin.alerts.strategy;

import java.util.Map;

import org.openspaces.admin.internal.alerts.strategy.MachineCpuUtilizationAlertStrategyBean;
import org.openspaces.core.util.StringProperties;

public class MachineCpuUtilizationAlertStrategyConfig implements AlertStrategyConfig {
	private static final String MOVING_AVERAGE_PERIOD = "movingAveragePeriod";
	private static final String LOW_THRESHOLD_PERC = "lowThresholdPerc";
	private static final String HIGH_THRESHOLD_PERC = "highThresholdPerc";
	private static final long serialVersionUID = 1L;
	private final StringProperties properties = new StringProperties();

	public MachineCpuUtilizationAlertStrategyConfig() {
	    setDefaultProperies();
	}
	
	private void setDefaultProperies() {
	    setHighThresholdPerc(Integer.MAX_VALUE);
        setLowThresholdPerc(0);
        setMovingAveragePeriod(60); //5 minute sliding window
	}
	
	public MachineCpuUtilizationAlertStrategyConfig setHighThresholdPerc(int highThreshold) {
	    properties.putInteger(HIGH_THRESHOLD_PERC, highThreshold);
		return this;
	}
	
	public int getHighThresholdPerc() {
	    return Integer.valueOf(properties.get(HIGH_THRESHOLD_PERC)).intValue();
	}
	
	public MachineCpuUtilizationAlertStrategyConfig setLowThresholdPerc(int lowThreshold) {
	    properties.putInteger(LOW_THRESHOLD_PERC, lowThreshold);
		return this;
	}
	
	public int getLowThresholdPerc() {
	    return Integer.valueOf(properties.get(LOW_THRESHOLD_PERC)).intValue();
	}
	
	public MachineCpuUtilizationAlertStrategyConfig setMovingAveragePeriod(int period) {
	    properties.putInteger(MOVING_AVERAGE_PERIOD, period);
		return this;
	}
	
	public int getMovingAveragePeriod() {
	    return Integer.valueOf(properties.get(MOVING_AVERAGE_PERIOD)).intValue();
	}
	
	public void applyRecommendedSettings() {
	    setLowThresholdPerc(60);
        setHighThresholdPerc(80);
        setMovingAveragePeriod(10);
	}
	
	public void setProperties(Map<String, String> properties) {
	    this.properties.clear();
	    this.setDefaultProperies();
	    this.properties.putAll(properties);
	}

	public Map<String, String> getProperties() {
		return properties.getProperties();
	}
	
	public String getStartegyBeanClassName() {
		return MachineCpuUtilizationAlertStrategyBean.class.getName();
	}

}
