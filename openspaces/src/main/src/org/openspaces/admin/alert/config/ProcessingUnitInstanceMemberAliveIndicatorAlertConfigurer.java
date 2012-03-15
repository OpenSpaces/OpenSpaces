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

/**
 * A processing unit instance fault-detection configurer. An alert is raised if the fault-detection
 * mechanism has detected failure of a processing unit instance. An alert is resolved if the
 * fault-detection mechanism succeeded to monitor a suspected processing unit instance.
 * <p>
 * <b>note:</b> The member alive fault-detection mechanism (retries and timeout) should be configured separately
 * in the pu.xml (see os-sla:member-alive-indicator)
 * <p>
 * Use the call to {@link #create()} to create a fully initialized
 * {@link ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration} configuration.
 * 
 * @see ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration
 * @since 8.0.6
 * 
 * @author Moran Avigdor
 */
public class ProcessingUnitInstanceMemberAliveIndicatorAlertConfigurer implements AlertConfigurer {

    private final ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration config = new ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration();
    
    /**
     * Constructs an empty provision failure alert configuration.
     */
    public ProcessingUnitInstanceMemberAliveIndicatorAlertConfigurer() {
    }
    
    /*
     * (non-Javadoc)
     * @see org.openspaces.admin.alert.config.AlertConfigurer#enable(boolean)
     */
    @Override
    public ProcessingUnitInstanceMemberAliveIndicatorAlertConfigurer enable(boolean enabled) {
        config.setEnabled(enabled);
        return this;
    }
    
    /**
     * Get a fully configured fault-detection alert configuration (after all properties have been set).
     * @return a fully configured alert configuration.
     */
    @Override
    public AlertConfiguration create() {
        return config;
    }
}
