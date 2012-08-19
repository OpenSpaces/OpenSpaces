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
package org.openspaces.admin.zone.config;

import java.util.Map;

/**
 * Satisfied if at least one of the zones exist
 * 
 * PU[A,B] is statisfied by GSC[A,B]  
 * PU[A,B] is statisfied by GSC[A]
 * PU[A,B] is statisfied by GSC[B]
 * PU[A]   is statisfied by GSC[A,B]
 * PU[A]   is statisfied by GSC[A]
 * PU[A]   is NOT statisfied by GSC[]
 * 
 * @author elip
 * @since 9.1.0
 */
public class AtLeastOneZoneConfig 
    extends RequiredZonesConfig {

    public AtLeastOneZoneConfig(Map<String, String> properties) {
        super(properties);
    }
 
    public AtLeastOneZoneConfig() {
        super();
    }

    @Override
    public boolean isSatisfiedBy(ExactZonesConfig existingZoneStatisticsConfig) {

        if (this.getZones().isEmpty()) {
            throw new IllegalStateException("No zones defined");
        }
        
        return this.getZones().removeAll(existingZoneStatisticsConfig.getZones());
    }
}
