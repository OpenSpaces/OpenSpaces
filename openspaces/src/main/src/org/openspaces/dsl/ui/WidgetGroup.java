package org.openspaces.dsl.ui;

import java.io.Serializable;
import java.util.List;

public class WidgetGroup implements Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String name;
	private List<Widget> widgets;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Widget> getWidgets() {
		return widgets;
	}
	public void setWidgets(List<Widget> widgets) {
		this.widgets = widgets;
	}
	public WidgetGroup(String name, List<Widget> widgets) {
		super();
		this.name = name;
		this.widgets = widgets;
	}
	public WidgetGroup() {
		super();
	}
	
}

