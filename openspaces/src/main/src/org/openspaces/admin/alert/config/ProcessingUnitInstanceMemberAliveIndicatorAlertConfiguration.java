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

import org.openspaces.admin.internal.alert.bean.ProcessingUnitInstanceMemberAliveIndicatorAlertBean;

/**
 * A processing unit instance fault-detection configuration. An alert is raised if the
 * fault-detection mechanism has detected failure of a processing unit instance. An alert is
 * resolved if the fault-detection mechanism succeeded to monitor a suspected processing unit
 * instance.
 * <p>
 * <b>note:</b> The member alive fault-detection mechanism (retries and timeout) should be configured separately
 * in the pu.xml (see os-sla:member-alive-indicator)
 * 
 * @see ProcessingUnitInstanceMemberAliveIndicatorAlertConfigurer
 * @since 8.0.6
 * 
 * @author Moran Avigdor
 */
public class ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration implements AlertConfiguration {
    private static final long serialVersionUID = 1L;
    
    private final Map<String,String> properties = new HashMap<String, String>();

    private boolean enabled;
    
    /**
     * Constructs an empty processing unit instance member alive indicator alert configuration.
     */
    public ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration() {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBeanClassName() {
        return ProcessingUnitInstanceMemberAliveIndicatorAlertBean.class.getName();
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
    public void setProperties(Map<String, String> properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

}
