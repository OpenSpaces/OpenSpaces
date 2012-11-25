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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gigaspaces.datasource.DataSourceQuery;
import com.gigaspaces.sync.AddIndexData;
import com.gigaspaces.sync.DataSyncOperation;
import com.gigaspaces.sync.IntroduceTypeData;

/**
 * A simple log based implementation of the {@link CassandraErrorHandler}.
 * 
 * @since 9.5
 * @author Dan Kilman
 */
public class DefaultCassandraErrorHandler implements CassandraErrorHandler {

    private static final Log logger = LogFactory.getLog(DefaultCassandraErrorHandler.class);
    
    @Override
    public void onDataSyncOperationError(
            DataSyncOperation dataSyncOperation,
            InvalidDataSyncOperationReason reason,
            Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error("Failed data sync operation: " + dataSyncOperation + ", reason: " + reason, t);
        }
    }

    @Override
    public void onAddIndexError(AddIndexData addIndexData, Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error("Failed adding indexes: " + addIndexData, t);
        }
    }

    @Override
    public void onIntroduceTypeError(IntroduceTypeData introduceTypeData, Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error("Failed introducing type: " + introduceTypeData, t);
        }
    }

    @Override
    public void onGetDataIteraotrError(DataSourceQuery dataSourceQuery,
            GetDataIteratorErrorReason reason, Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error("Failed get data iterator for query: " + dataSourceQuery + 
                        ", reason: " + reason, t);
        }
    }
    
}
