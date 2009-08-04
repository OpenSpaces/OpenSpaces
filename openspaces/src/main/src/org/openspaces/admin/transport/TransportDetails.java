/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.transport;

/**
 * Details (non changeable information) of a single transport.
 *
 * @author kimchy
 */
public interface TransportDetails {

    /**
     * Returns the local host address of the transport.
     *
     * @see java.net.InetAddress#getLocalHost()#getHostAddress()
     */
    String getHostAddress();

    /**
     * Returns the local host address of the transport.
     *
     * @see java.net.InetAddress#getLocalHost()#getHostName()
     */
    String getHostName();

    /**
     * Returns the host name or address the communication layer bounded on.
     */
    String getBindHost();

    /**
     * Returns the host name or address the communication layer bounded on.
     */
    int getPort();

    /**
     * Returns the minimum number of threads configured for the transport communication
     * layer thread pool.
     */
    int getMinThreads();

    /**
     * Returns the maximum number of threads configured for the transport communication
     * layer thread pool.
     */
    int getMaxThreads();

    /**
     * Returns <code>true</code> if ssl is enabled for the transport.
     */
    boolean isSslEnabled();
}
