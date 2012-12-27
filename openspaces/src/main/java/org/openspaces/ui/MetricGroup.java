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

public class MetricGroup implements Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String name;
	public MetricGroup() {
		super();
	}
	public MetricGroup(String name, List<Object> metrics) {
		super();
		this.name = name;
		this.metrics = metrics;
	}
	private List<Object> metrics;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Object> getMetrics() {
		return metrics;
	}
	public void setMetrics(List<Object> metrics) {
		this.metrics = metrics;
	}
}
