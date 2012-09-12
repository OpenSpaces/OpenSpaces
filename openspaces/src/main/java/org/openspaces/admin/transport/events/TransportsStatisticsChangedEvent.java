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

import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.transport.TransportsStatistics;

/**
 * An event indicating that an aggregated transports level statistics has changed.
 *
 * <p>Note, monitoring needs to be enabled in order to receive the events.
 *
 * @author kimchy
 */
public class TransportsStatisticsChangedEvent {

    private final Transports transports;

    private final TransportsStatistics statistics;

    public TransportsStatisticsChangedEvent(Transports transports, TransportsStatistics statistics) {
        this.transports = transports;
        this.statistics = statistics;
    }

    /**
     * Returns the tranports associated with this events.
     */
    public Transports getTransports() {
        return transports;
    }

    /**
     * Returns the aggregated transports statistics sampled.
     */
    public TransportsStatistics getStatistics() {
        return statistics;
    }
}