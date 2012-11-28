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
package org.openspaces.persistency.cassandra.pool;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.j_spaces.kernel.pool.IResource;
import com.j_spaces.kernel.pool.Resource;

/**
 * A {@link IResource} representing a {@link Connection}.
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public class ConnectionResource extends Resource
        implements IResource {
    
    private static final Log logger = LogFactory.getLog(ConnectionResource.class);

    private final Connection connection;

    public ConnectionResource(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public void clear() {   
        if (!isFromPool()) {
            try {
                connection.close();
            } catch (SQLException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed closing jdbc connection", e);
                }
            }
        }
    }

    /**
     * @return The underlying {@link Connection} represented by this {@link ConnectionResource}
     */
    public Connection getConnection() {
        return connection;
    }
}
