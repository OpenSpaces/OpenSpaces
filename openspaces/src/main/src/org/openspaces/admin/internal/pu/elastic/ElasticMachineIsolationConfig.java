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

import org.openspaces.core.util.StringProperties;

/**
 * @author Moran Avigdor
 * @since 8.0.1
 */
public class ElasticMachineIsolationConfig {
    
    private static final String ELASTIC_MACHINE_ISOLATION_SHARING_ID_KEY = "elastic-machine-isolation-sharing-id";
    private static final String ELASTIC_MACHINE_ISOLATION_PUBLIC_ID_KEY = "elastic-machine-isolation-public-id";
    private final StringProperties properties;

    public ElasticMachineIsolationConfig(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }
    
    public boolean isDedicatedIsolation() {
        return (!isSharedIsolation() && !isPublicMachineIsolation());
    }
    
    public boolean isSharedIsolation() {
        return getSharingId() != null;
    }
    
    public boolean isPublicMachineIsolation() {
        return (properties.getBoolean(ELASTIC_MACHINE_ISOLATION_PUBLIC_ID_KEY, false));
    }
    
    public String getSharingId() {
        return properties.get(ELASTIC_MACHINE_ISOLATION_SHARING_ID_KEY);
    }

    public void setSharingId(String sharingId) {
        if (sharingId == null) {
            throw new IllegalArgumentException("sharingId cannot be null");
        }
        properties.put(ELASTIC_MACHINE_ISOLATION_SHARING_ID_KEY, sharingId);
        properties.putBoolean(ELASTIC_MACHINE_ISOLATION_PUBLIC_ID_KEY, false);
    }
    
    public void setPublic() {
        properties.remove(ELASTIC_MACHINE_ISOLATION_SHARING_ID_KEY);
        properties.putBoolean(ELASTIC_MACHINE_ISOLATION_PUBLIC_ID_KEY, true);
    }
    
    public void setDedicated() {
        properties.remove(ELASTIC_MACHINE_ISOLATION_SHARING_ID_KEY);
        properties.putBoolean(ELASTIC_MACHINE_ISOLATION_PUBLIC_ID_KEY, false);
    }
}
