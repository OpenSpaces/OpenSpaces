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
