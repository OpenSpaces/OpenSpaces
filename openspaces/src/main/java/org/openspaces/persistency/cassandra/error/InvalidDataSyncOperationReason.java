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
package org.openspaces.persistency.cassandra.error;

import org.openspaces.persistency.cassandra.CassandraSpaceSynchronizationEndpoint;
import org.openspaces.persistency.cassandra.meta.conversion.ColumnFamilyNameConverter;
import org.openspaces.persistency.cassandra.meta.data.ColumnFamilyRow;

import com.gigaspaces.sync.DataSyncOperation;

/**
 * An enum containing reasons why a certain {@link DataSyncOperation} recieved in a call to
 * {@link CassandraSpaceSynchronizationEndpoint#onTransactionSynchronization(com.gigaspaces.sync.TransactionData)}
 * or 
 * {@link CassandraSpaceSynchronizationEndpoint#onOperationsBatchSynchronization(com.gigaspaces.sync.OperationsBatchData)}
 * could not be operated on.
 * 
 * @since 9.5
 * @author Dan Kilman
 */
public enum InvalidDataSyncOperationReason {
    
    /**
     * {@link DataSyncOperation} could not be converted to a space document.
     */
    NotSupportsAsDocument, 
    
    /**
     * {@link DataSyncOperation} corresonds to a nonexistent type name.
     * (which corresponds to a column family, @see {@link ColumnFamilyNameConverter})
     */
    MissingColumnFamily, 
    
    /**
     * {@link DataSyncOperation#getDataSyncOperationType()} is not supported.
     */
    UnsupportedDataSyncOperation,
    
    /**
     * {@link DataSyncOperation} value does not contain an id property value.
     */
    MissingIDValue,
    
    /**
     * {@link CassandraTypeIntrospectionException} occured during conversion of the entry to a {@link ColumnFamilyRow}.
     */
    TypeIntrospectionException
    
}
