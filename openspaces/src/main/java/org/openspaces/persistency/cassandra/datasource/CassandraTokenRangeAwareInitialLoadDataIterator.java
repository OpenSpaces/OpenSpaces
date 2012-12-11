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
package org.openspaces.persistency.cassandra.datasource;

import java.util.Collection;
import java.util.Iterator;

import org.openspaces.persistency.cassandra.CassandraConsistencyLevel;
import org.openspaces.persistency.cassandra.meta.ColumnFamilyMetadata;
import org.openspaces.persistency.cassandra.meta.mapping.SpaceDocumentColumnFamilyMapper;
import org.openspaces.persistency.cassandra.pool.ConnectionResource;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.document.SpaceDocument;

/**
 * @since 9.1.1
 * @author Dan Kilman
 */
public class CassandraTokenRangeAwareInitialLoadDataIterator implements DataIterator<Object> {
    
    private final SpaceDocumentColumnFamilyMapper mapper;
    private final Iterator<ColumnFamilyMetadata>  metadata;
    private final ConnectionResource              connectionResource;
    private final int                             batchLimit;
    private final CassandraConsistencyLevel       readConsistencyLevel;

    private CassandraTokenRangeAwareDataIterator  currentIterator;
    
    public CassandraTokenRangeAwareInitialLoadDataIterator(
            SpaceDocumentColumnFamilyMapper mapper, 
            Collection<ColumnFamilyMetadata> metadata,
            ConnectionResource connectionResource,
            int batchLimit, 
            CassandraConsistencyLevel readConsistencyLevel) {
        this.mapper = mapper;
        this.batchLimit = batchLimit;
        this.connectionResource = connectionResource;
        this.readConsistencyLevel = readConsistencyLevel;
        this.metadata = metadata.iterator();
        this.currentIterator = nextDataIterator();
    }
    
    @Override
    public boolean hasNext() {
        while (currentIterator != null && !currentIterator.hasNext()) {
            currentIterator = nextDataIterator();
        }

        return currentIterator != null;
    }

    @Override
    public SpaceDocument next() {
        return currentIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove is not supported for this iterator");
    }

    @Override
    public void close() {
        if (currentIterator != null) {
            currentIterator.closeSelfResources();
        }
        connectionResource.release();
    }

    private CassandraTokenRangeAwareDataIterator nextDataIterator() {
        if (metadata.hasNext()) {
            ColumnFamilyMetadata metadata = this.metadata.next();
            return new CassandraTokenRangeAwareDataIterator(mapper,
                                                            metadata,
                                                            connectionResource, 
                                                            null /* the null value will be interperted as 
                                                            select all */,
                                                            Integer.MAX_VALUE,
                                                            batchLimit,
                                                            readConsistencyLevel);
        } else {
            return null;
        }
    }
}
