package org.openspaces.dsl.ui;

public class AbstractBasicWidget implements Widget {

	private String metric;

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public AbstractBasicWidget(String metric) {
		super();
		this.metric = metric;
	}

	public AbstractBasicWidget() {
		super();
	}
	
}
