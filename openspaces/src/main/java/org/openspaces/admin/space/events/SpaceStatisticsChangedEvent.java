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

package org.openspaces.admin.space.events;

import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceStatistics;

/**
 * An event indicating that a Space level statistics has changed.
 *
 * <p>Note, monitoring needs to be enabled in order to receive the events.
 *
 * @author kimchy
 * @see org.openspaces.admin.space.events.SpaceStatisticsChangedEventListener
 * @see org.openspaces.admin.space.events.SpaceStatisticsChangedEventManager
 */
public class SpaceStatisticsChangedEvent {

    private final Space space;

    private final SpaceStatistics statistics;

    public SpaceStatisticsChangedEvent(Space space, SpaceStatistics statistics) {
        this.space = space;
        this.statistics = statistics;
    }

    /**
     * Returns a the Space associated with the event.
     */
    public Space getSpace() {
        return space;
    }

    /**
     * Returns the statistics of the Space sampled.
     */
    public SpaceStatistics getStatistics() {
        return statistics;
    }
}