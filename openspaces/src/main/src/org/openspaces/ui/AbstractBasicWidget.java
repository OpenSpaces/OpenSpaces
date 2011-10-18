package org.openspaces.ui;

import java.io.Serializable;

public class AbstractBasicWidget implements Widget, Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
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
