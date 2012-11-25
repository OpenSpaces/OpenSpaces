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

import org.openspaces.persistency.cassandra.CassandraSpaceDataSource;
import org.openspaces.persistency.cassandra.meta.conversion.ColumnFamilyNameConverter;

import com.gigaspaces.datasource.DataSourceSQLQuery;
import com.gigaspaces.document.SpaceDocument;

/**
 * An enum containing {@link CassandraSpaceDataSource#getDataIterator(com.gigaspaces.datasource.DataSourceQuery)}
 * failure reasons.
 * 
 * @since 9.5
 * @author Dan Kilman
 */
public enum GetDataIteratorErrorReason {
    
    /**
     * Data iterator was requested for a non existent type name 
     * (which corresponds to a column family, @see {@link ColumnFamilyNameConverter})
     */
    MissingColumnFamily,
    
    /**
     * Date iterator was requested with a query that does not support being viewed a {@link SpaceDocument}
     * nor a {@link DataSourceSQLQuery}.
     */
    UnsupportedDataSourceQuery,
    
    /**
     * An exception occured during query execution.
     */
    QueryExecutionException
    
}
