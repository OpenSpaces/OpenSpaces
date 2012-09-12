/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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

