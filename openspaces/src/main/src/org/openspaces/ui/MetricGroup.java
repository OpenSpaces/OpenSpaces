package org.openspaces.ui;

import java.io.Serializable;
import java.util.List;

public class MetricGroup implements Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String name;
	public MetricGroup() {
		super();
	}
	public MetricGroup(String name, List<String> metrics) {
		super();
		this.name = name;
		this.metrics = metrics;
	}
	private List<String> metrics;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getMetrics() {
		return metrics;
	}
	public void setMetrics(List<String> metrics) {
		this.metrics = metrics;
	}
}
