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
package org.openspaces.grid.gsm.strategy;

import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

/**
+ * The business logic that scales an elastic processing unit based on the specified
+ * {@link ManualCapacityScaleConfig}
+ * 
+ * @author itaif
+ * @since 8.0
+ */
public class ManualCapacityScaleStrategyBean extends AbstractCapacityScaleStrategyBean { 
            
    @Override
    public void afterPropertiesSet() {
        
        super.afterPropertiesSet();
        
        ManualCapacityScaleConfig manualCapacityScaleConfig = getConfig();
        
        setCapacityRequirementConfig(manualCapacityScaleConfig);
        setScaleStrategyConfig(manualCapacityScaleConfig);
        
    }
    
    @Override
    public void enforceSla() throws SlaEnforcementInProgressException {
        super.enforceCapacityRequirement();
    }
    
    public ManualCapacityScaleConfig getConfig() {
        return new ManualCapacityScaleConfig(super.getProperties());
    }

}
