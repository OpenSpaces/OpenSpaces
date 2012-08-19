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
package org.openspaces.admin.pu.elastic.config;

import org.openspaces.admin.zone.config.ExactZonesConfig;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 */
public interface ScaleStrategyAgentZonesAffinityConfig {

    public boolean isGridServiceAgentZonesAffinity();
    
    /**
     * By default disabled, which means that the following machines:
     * - Machines started due to failover of another machine.
     * - Machines started by {@link AutomaticCapacityScaleRuleConfig} sue to threshold breach 
     * are started with a Grid Service Agent zones as described in {@link #getGridServiceAgentZones()}
     * 
     * When enabled, these machines are started with the same {@link ExactZonesConfig} as the failed machine, 
     * or the same {@link ExactZonesConfig} as the machines whose threshold was breached.
     *   
     * @since 9.1.0
     */
    public void setGridServiceAgentZonesAffinity(boolean enableAgentZonesAffinity);

}
