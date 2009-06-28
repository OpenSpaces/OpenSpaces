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

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEventManager;
import org.openspaces.admin.transport.events.TransportsStatisticsChangedEventManager;

/**
 * Transports hold all the different {@link org.openspaces.admin.transport.Transport}s that are currently
 * siscovered.
 *
 * <p>Provides simple means to get all the current transports, as well as as registering for
 * transport lifecycle (added and removed) events.
 *
 * <p>Provides the ability to start a statistics monitor on all current transports using
 * {@link #startStatisticsMonitor()}. Newly discovered transports will automatically use
 * the statistics monitor as well.
 *
 * @author kimchy
 */
public interface Transports extends Iterable<Transport>, AdminAware, StatisticsMonitor {

    /**
     * Returns all the currently discovered transports.
     */
    Transport[] getTransports();

    /**
     * Returns all the transports bounded on the specified host.
     *
     * @see org.openspaces.admin.transport.Transport#getBindHost()
     */
    Transport[] getTransports(String host);

    /**
     * Returns the transport that is bounded on the specified host and port.
     *
     * @see org.openspaces.admin.transport.Transport#getBindHost()
     * @see Transport#getPort()
     */
    Transport getTransportByHostAndPort(String host, int port);

    /**
     * Returns the transport based on the specified UID.
     */
    Transport getTransportByUID(String uid);

    /**
     * Returns the number of currently discovered transports.
     */
    int getSize();

    /**
     * Returns the aggregated details (non changeable) of all the currently discovered transports.
     */
    TransportsDetails getDetails();

    /**
     * Returns the aggregated statistics of all the currently discovered transports.
     */
    TransportsStatistics getStatistics();

    /**
     * Allows to register for aggregated {@link org.openspaces.admin.transport.events.TransportsStatisticsChangedEvent}s.
     *
     * <p>Note, the transports must be in a monitoring state in order to receive the events.
     */
    TransportsStatisticsChangedEventManager getStatisticsChanged();

    /**
     * Allows to register for transport level {@link org.openspaces.admin.transport.events.TransportStatisticsChangedEvent}s.
     *
     * <p>Note, the transports must be in a monitoring state in order to receive the events.
     */
    TransportStatisticsChangedEventManager getTransportStatisticsChanged();
}
