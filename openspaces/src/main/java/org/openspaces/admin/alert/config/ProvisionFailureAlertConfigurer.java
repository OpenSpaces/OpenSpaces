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
 * A provision failure alert configurer. An alert is raised if the processing unit has less actual
 * instances than planned instances. An alert is resolved when the processing unit actual instance
 * count is equal to the planned instance count.
 * <p>
 * Use the call to {@link #create()} to create a fully initialized
 * {@link ProvisionFailureAlertConfiguration} configuration.
 * 
 * @see ProvisionFailureAlertConfiguration
 * 
 * @author Moran Avigdor
 * @since 8.0.3
 */
public class ProvisionFailureAlertConfigurer implements AlertConfigurer {

    private final ProvisionFailureAlertConfiguration config = new ProvisionFailureAlertConfiguration();
    
    /**
     * Constructs an empty provision failure alert configuration.
     */
    public ProvisionFailureAlertConfigurer() {
    }
    
    /*
     * (non-Javadoc)
     * @see org.openspaces.admin.alert.config.AlertConfigurer#enable(boolean)
     */
    @Override
    public ProvisionFailureAlertConfigurer enable(boolean enabled) {
        config.setEnabled(enabled);
        return this;
    }
    
    /**
     * Get a fully configured provision failure alert configuration (after all properties have been set).
     * @return a fully configured alert configuration.
     */
    @Override
    public AlertConfiguration create() {
        return config;
    }

}
