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

/**
 * A configurer for creating {@link HectorCassandraClient} instances.
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public class HectorCassandraClientConfigurer {

    private String host;
    private int    port;
    private String clusterName;
    private String keyspaceName;
    
    /**
     * Sets the host name of the Cassandra cluster to connect to.
     * @param host The host.
     * @return {@code this} instance.
     */
    public HectorCassandraClientConfigurer host(String host) {
        this.host = host;
        return this;
    }
    
    /**
     * Sets the port matching the host name of the Cassandra cluster to
     * connect to.
     * @param port The port.
     * @return {@code this} instance.
     */
    public HectorCassandraClientConfigurer port(int port) {
        this.port = port;
        return this;
    }
    
    /**
     * Sets the keyspaceName of the keyspace to connect to.
     * @param keyspaceName The keyspace name.
     * @return {@code this} instance.
     */
    public HectorCassandraClientConfigurer keyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
        return this;
    }
    
    /**
     * Sets the cluster name used internally by the hector library.
     * Use this if you plan to connect to more than one cassandra cluster
     * in the same JVM.
     * @param clusterName (Optional) The cluster 'tag' name.
     * @return {@code this} instance.
     */
    public HectorCassandraClientConfigurer clusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }
    
    /**
     * @return An instance of {@link HectorCassandraClient} matching this configurer
     * configuration.
     */
    public HectorCassandraClient create() {
        return new HectorCassandraClient(host, port, keyspaceName, clusterName);
    }
    
}
