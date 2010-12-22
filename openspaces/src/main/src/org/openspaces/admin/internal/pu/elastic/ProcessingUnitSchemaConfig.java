package org.openspaces.admin.internal.pu.elastic;

import java.util.Map;

import org.openspaces.core.util.StringProperties;

public class ProcessingUnitSchemaConfig {
    
    private static final String ASYNC_REPLICATED_SCHEMA = "async_replicated";
    private static final String SYNC_REPLICATED_SCHEMA = "sync_replicated";
    private static final String PARTITIONED_SYNC2BACKUP_SCHEMA = "partitioned-sync2backup";
    private static final String SCHEMA_KEY = "schema";
    private static final String DEFAULT_SCHEMA = "default";
    
    private StringProperties elasticProperties;
    
    public ProcessingUnitSchemaConfig(Map<String,String> elasticProperties) {
        this.elasticProperties = new StringProperties(elasticProperties);
    }
    
    public boolean isPartitionedSync2BackupSchema() {
        return isSchema(PARTITIONED_SYNC2BACKUP_SCHEMA);
    }

    public void setDefaultSchema() {
        setSchema(DEFAULT_SCHEMA);
    }
    
    public boolean isDefaultSchema() {
        return isSchema(DEFAULT_SCHEMA);
    }
    
    public void setPartitionedSync2BackupSchema() {
        setSchema(PARTITIONED_SYNC2BACKUP_SCHEMA);
    }

    public void setAsyncReplicatedSchema() {
        setSchema(ASYNC_REPLICATED_SCHEMA);
    }
    
    public boolean isAsyncReplicatedSchema() {
        return isSchema(ASYNC_REPLICATED_SCHEMA);
    }
    
    public boolean isSyncReplicatedSchema() {
        return isSchema(SYNC_REPLICATED_SCHEMA);
    }
    public void setSyncReplicatedSchema() {
        setSchema(SYNC_REPLICATED_SCHEMA);
    }
    
    private void setSchema(String schema) {
        elasticProperties.put(SCHEMA_KEY, schema);
    }

    private boolean isSchema(String schema) {
        return schema.equals(getSchema());
    }

    public String getSchema() {
        return elasticProperties.get(SCHEMA_KEY,DEFAULT_SCHEMA);
    }

}
