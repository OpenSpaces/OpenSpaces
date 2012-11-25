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

import com.gigaspaces.datasource.DataSourceQuery;
import com.gigaspaces.sync.AddIndexData;
import com.gigaspaces.sync.DataSyncOperation;
import com.gigaspaces.sync.IntroduceTypeData;

/**
 * An interface for handing errors that occured during schema update operations,
 * batch operations and read operation.
 * @since 9.5
 * @author Dan Kilman
 */
public interface CassandraErrorHandler {
    
    /**
     * Called when a {@link DataSyncOperation} could not be used.
     * @param dataSyncOperation The invalid {@link DataSyncOperation}.
     * @param reason The reason.
     * @param t The exceptoin if any occured, null oterwise.
     */
    void onDataSyncOperationError(
            DataSyncOperation dataSyncOperation, 
            InvalidDataSyncOperationReason reason,
            Throwable t);

    /**
     * Called when an exception occured during dynamic secondary index addition.
     * @param addIndexData the {@link AddIndexData} involved in the error.
     * @param t The exception that occured during the operation.
     */
    void onAddIndexError(AddIndexData addIndexData, Throwable t);

    /**
     * Called when an exception occured during type introduction.
     * @param introduceTypeData The {@link IntroduceTypeData} invovled in the error.
     * @param t The exception that occured during the operation.
     */
    void onIntroduceTypeError(IntroduceTypeData introduceTypeData, Throwable t);
    
    /**
     * Called when a query could not be executed.
     * @param dataSourceQuery The query.
     * @param reason The reason.
     * @param t The exception if any occured, null otherwise.
     */
    void onGetDataIteraotrError(
            DataSourceQuery dataSourceQuery,
            GetDataIteratorErrorReason reason,
            Throwable t);
    
}
