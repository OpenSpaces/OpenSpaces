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
package org.openspaces.utest.admin.alerts;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.alert.config.AlertConfiguration;

public class MockAlertConfiguration implements AlertConfiguration {

    private boolean enabled;
    private Map<String, String> properties = new HashMap<String, String>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

    }

    public String getBeanClassName() {
        return MockAlertBean.class.getName();
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;

    }
    
    public void setHighThreshold(int highThreshold) {
        properties.put("high-threshold", String.valueOf(highThreshold));
    }
    
    public Integer getHighThreshold() {
        return Integer.valueOf(properties.get("high-threshold"));
    }
    
    public void setLowThreshold(int lowThreshold) {
        properties.put("low-threshold", String.valueOf(lowThreshold));
    }
    
    public Integer getLowThreshold() {
        return Integer.valueOf(properties.get("low-threshold"));
    }
}
