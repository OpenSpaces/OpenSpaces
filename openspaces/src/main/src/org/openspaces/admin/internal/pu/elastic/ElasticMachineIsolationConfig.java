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
package org.openspaces.admin.internal.pu.elastic;

import java.util.Map;

/**
 * @author Moran Avigdor
 * @since 8.0.1
 */
public class ElasticMachineIsolationConfig {
    
    private static final String ELASTIC_MACHINE_ISOLATION_SHARING_ID_KEY = "elastic-machine-isolation-sharing-id";
    private final Map<String, String> properties;

    public ElasticMachineIsolationConfig(Map<String, String> properties) {
        this.properties = properties;
    }
    
    public boolean isDedicatedIsolation() {
        return (getSharingId() == null);
    }
    
    public boolean isSharedIsolation() {
        return (getSharingId() != null);
    }
    
    public String getSharingId() {
        return properties.get(ELASTIC_MACHINE_ISOLATION_SHARING_ID_KEY);
    }

    public void setSharingId(String sharingId) {
        if (sharingId == null) {
            throw new IllegalArgumentException("sharingId cannot be null");
        }
        properties.put(ELASTIC_MACHINE_ISOLATION_SHARING_ID_KEY, sharingId);
    }

    public void setDedicated() {
        properties.remove(ELASTIC_MACHINE_ISOLATION_SHARING_ID_KEY);
    }
}
