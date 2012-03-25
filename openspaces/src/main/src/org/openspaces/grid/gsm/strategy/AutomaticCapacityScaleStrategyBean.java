/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.grid.gsm.strategy;

import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyCapacityRequirementConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

/**
+ * The business logic that scales an elastic processing unit based on the specified
+ * {@link AutomaticCapacityScaleConfig}
+ * 
+ * @author itaif
+ * @since 9.0.0
+ */
public class AutomaticCapacityScaleStrategyBean extends AbstractCapacityScaleStrategyBean{
    
    // created by afterPropertiesSet()
    private AutomaticCapacityScaleConfig automaticCapacityScaleConfig;
    
    @Override
    public void afterPropertiesSet() {
        
        super.afterPropertiesSet();
        
        this.automaticCapacityScaleConfig = new AutomaticCapacityScaleConfig(super.getProperties());
        
        ScaleStrategyCapacityRequirementConfig manualCapacity = automaticCapacityScaleConfig.getMinCapacity();
        ScaleStrategyConfig scaleStrategy = automaticCapacityScaleConfig;
        super.setManualCapacityScaleConfig(manualCapacity,scaleStrategy);
    }
    
    @Override
    public ScaleStrategyConfig getConfig() {
        return automaticCapacityScaleConfig;
    }

    @Override
    protected void enforceSla() throws SlaEnforcementInProgressException {
        super.enforceManualCapacityScaleConfig();
    }

}
