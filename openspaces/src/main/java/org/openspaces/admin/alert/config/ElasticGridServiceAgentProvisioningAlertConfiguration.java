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
package org.openspaces.admin.alert.config;
import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.alert.bean.ElasticGridServiceAgentProvisioningAlertBean;

public class ElasticGridServiceAgentProvisioningAlertConfiguration implements AlertConfiguration {

    private static final long serialVersionUID = 1L;

    
    private final Map<String,String> properties = new HashMap<String, String>();

    private boolean enabled;

    /**
     * Constructs an empty machine CPU utilization alert configuration.
     */
    public ElasticGridServiceAgentProvisioningAlertConfiguration() {
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBeanClassName() {
        return ElasticGridServiceAgentProvisioningAlertBean.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
