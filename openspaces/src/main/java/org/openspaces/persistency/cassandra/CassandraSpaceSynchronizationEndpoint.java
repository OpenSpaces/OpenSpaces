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
package org.openspaces.persistency.cassandra;

import java.util.LinkedList;
import java.util.List;

import org.openspaces.persistency.cassandra.error.SpaceCassandraDataSourceException;
import org.openspaces.persistency.cassandra.error.SpaceCassandraSynchronizationException;
import org.openspaces.persistency.cassandra.meta.ColumnFamilyMetadata;
import org.openspaces.persistency.cassandra.meta.conversion.ColumnFamilyNameConverter;
import org.openspaces.persistency.cassandra.meta.data.ColumnFamilyRow;
import org.openspaces.persistency.cassandra.meta.data.ColumnFamilyRow.ColumnFamilyRowType;
import org.openspaces.persistency.cassandra.meta.mapping.DefaultSpaceDocumentColumnFamilyMapper;
import org.openspaces.persistency.cassandra.meta.mapping.SpaceDocumentColumnFamilyMapper;
import org.openspaces.persistency.cassandra.meta.mapping.filter.FlattenedPropertiesFilter;
import org.openspaces.persistency.cassandra.meta.types.dynamic.PropertyValueSerializer;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.index.SpaceIndex;
import com.gigaspaces.metadata.index.SpaceIndexType;
import com.gigaspaces.sync.AddIndexData;
import com.gigaspaces.sync.DataSyncOperation;
import com.gigaspaces.sync.IntroduceTypeData;
import com.gigaspaces.sync.OperationsBatchData;
import com.gigaspaces.sync.SpaceSynchronizationEndpoint;
import com.gigaspaces.sync.TransactionData;

/**
 * 
 * A Cassandra implementation of {@link SpaceSynchronizationEndpoint}.
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public class CassandraSpaceSynchronizationEndpoint
        extends SpaceSynchronizationEndpoint {
    
    private final SpaceDocumentColumnFamilyMapper mapper;
    private final HectorCassandraClient           hectorClient;

    public CassandraSpaceSynchronizationEndpoint(int maxNestingLevel,
            PropertyValueSerializer fixedPropertyValueSerializer,
            PropertyValueSerializer dynamicPropertyValueSerializer,
            FlattenedPropertiesFilter flattenedPropertiesFilter,
            ColumnFamilyNameConverter columnFamilyNameConverter,
            HectorCassandraClient hectorClient) {
        
        if (hectorClient == null) {
            throw new IllegalArgumentException("hectorClient must be set");
        }
        
        this.hectorClient = hectorClient;
        this.hectorClient.createMetadataColumnFamilyColumnFamilyIfNecessary();
        
        mapper = new DefaultSpaceDocumentColumnFamilyMapper(fixedPropertyValueSerializer,
                                                            dynamicPropertyValueSerializer,                                                             
                                                            flattenedPropertiesFilter, 
                                                            columnFamilyNameConverter,
                                                            maxNestingLevel);
    }

    @Override
    public void onTransactionSynchronization(TransactionData transactionData) {
        doSynchronization(transactionData.getTransactionParticipantDataItems());
    }
    
    @Override
    public void onOperationsBatchSynchronization(OperationsBatchData batchData) {
        doSynchronization(batchData.getBatchDataItems());
    }

    private void doSynchronization(DataSyncOperation[] dataSyncOperations) {
        
        List<ColumnFamilyRow> rows = new LinkedList<ColumnFamilyRow>();
        
        for (DataSyncOperation dataSyncOperation : dataSyncOperations) {
            if (!dataSyncOperation.supportsDataAsDocument()) {
                throw new SpaceCassandraSynchronizationException("Data sync operation does not support asDocument", null);
            }
                        
            
            SpaceDocument spaceDoc = dataSyncOperation.getDataAsDocument();
            String typeName = spaceDoc.getTypeName();
            ColumnFamilyMetadata metadata = hectorClient.getColumnFamilyMetadata(typeName);

            if (metadata == null) {
                metadata = hectorClient.fetchColumnFamilyMetadata(typeName, mapper);
                if (metadata == null) {
                    throw new SpaceCassandraDataSourceException("Could not find column family for type name: "
                            + typeName, null);
                }
            }

            String keyName = metadata.getKeyName();
            Object keyValue = spaceDoc.getProperty(keyName);
                
            if (keyValue == null) {
                throw new SpaceCassandraSynchronizationException("Data sync operation missing id property value", null);
            }
            
            ColumnFamilyRow columnFamilyRow;
            switch(dataSyncOperation.getDataSyncOperationType()) {
                case WRITE:
                    columnFamilyRow = mapper.toColumnFamilyRow(metadata, 
                                                                spaceDoc, 
                                                                ColumnFamilyRowType.Write,
                                                                true /* useDynamicPropertySerializerForDynamicColumns*/);
                    break;
                case UPDATE:
                    columnFamilyRow = mapper.toColumnFamilyRow(metadata, 
                                                                spaceDoc, 
                                                                ColumnFamilyRowType.Update,
                                                                true /* useDynamicPropertySerializerForDynamicColumns*/);
                    break;
                case PARTIAL_UPDATE:
                    columnFamilyRow = mapper.toColumnFamilyRow(metadata, 
                                                               spaceDoc, 
                                                               ColumnFamilyRowType.PartialUpdate,
                                                               true /* useDynamicPropertySerializerForDynamicColumns*/);
                    break;
                case REMOVE:
                    columnFamilyRow = new ColumnFamilyRow(metadata, keyValue, ColumnFamilyRowType.Remove);
                    break;
                default:
                {
                    throw new IllegalStateException("Unsupported data sync operation type: " + 
                            dataSyncOperation.getDataSyncOperationType());
                }
            }
            
            rows.add(columnFamilyRow);
        }

        hectorClient.performBatchOperation(rows);
    }

    @Override
    public void onIntroduceType(IntroduceTypeData introduceTypeData) {
        
        ColumnFamilyMetadata columnFamilyMetadata = 
                mapper.toColumnFamilyMetadata(introduceTypeData.getTypeDescriptor());
        
        hectorClient.createColumnFamilyIfNecessary(columnFamilyMetadata,
                                                   true /* shouldPersist */);
    }
    
    @Override
    public void onAddIndex(AddIndexData addIndexData) {
        
        String typeName = addIndexData.getTypeName();
        List<String> indexes = new LinkedList<String>();
        for (SpaceIndex index : addIndexData.getIndexes()) {
            if (index.getIndexType() == SpaceIndexType.NONE) {
                continue;
            }
            indexes.add(index.getName());
         }
        
        if (indexes.isEmpty()) {
            return;
        }
        
        hectorClient.addIndexesToColumnFamily(typeName, indexes, mapper);
    }
    
}
