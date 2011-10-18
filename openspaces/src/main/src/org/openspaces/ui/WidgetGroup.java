package org.openspaces.ui;

import java.io.Serializable;
import java.util.List;

public class WidgetGroup implements Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String name;
    private String title;
	private List<Widget> widgets;
	public String getName() {
		return name;
	}
	public String getTitle(){
	    return title == null ? getName() : title;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setTtile(String title){
	    this.title = title;
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

