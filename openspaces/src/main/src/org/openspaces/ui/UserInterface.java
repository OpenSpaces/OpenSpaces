package org.openspaces.ui;

import java.io.Serializable;
import java.util.List;

public class UserInterface implements Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UserInterface() {
		super();
	}
	private List<MetricGroup> metricGroups;
	private List<WidgetGroup> widgetGroups;
	
	public UserInterface(List<MetricGroup> metricGroups, List<WidgetGroup> widgetGroups) {
		super();
		this.metricGroups = metricGroups;
		this.widgetGroups = widgetGroups;
	}
	public List<MetricGroup> getMetricGroups() {
		return metricGroups;
	}
	public void setMetricGroups(List<MetricGroup> metricGroups) {
		this.metricGroups = metricGroups;
	}
	public List<WidgetGroup> getWidgetGroups() {
		return widgetGroups;
	}
	public void setWidgetGroups(List<WidgetGroup> widgetGroups) {
		this.widgetGroups = widgetGroups;
	}
}
