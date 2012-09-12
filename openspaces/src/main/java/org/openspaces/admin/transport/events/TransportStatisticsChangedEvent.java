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

package org.openspaces.admin.transport.events;

import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.transport.TransportStatistics;

/**
 * An event indicating that a transport level statistics has changed.
 *
 * <p>Note, monitoring needs to be enabled in order to receive the events.
 *
 * @author kimchy
 * @see TransportStatisticsChangedEventManager
 * @see TransportStatisticsChangedEventListener
 *
 * @author kimchy
 */
public class TransportStatisticsChangedEvent {

    private final Transport transport;

    private final TransportStatistics statistics;

    public TransportStatisticsChangedEvent(Transport transport, TransportStatistics statistics) {
        this.transport = transport;
        this.statistics = statistics;
    }

    /**
     * Returns the associated transport with the event.
     */
    public Transport getTransport() {
        return transport;
    }

    /**
     * Returns the transport statistics sampled.
     */
    public TransportStatistics getStatistics() {
        return statistics;
    }
}