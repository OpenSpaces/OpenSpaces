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

package org.openspaces.admin.os;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventManager;
import org.openspaces.admin.os.events.OperatingSystemsStatisticsChangedEventManager;

import java.util.Map;

/**
 * Machines hold all the different {@link OperatingSystem}s that are currently
 * discovered.
 *
 * <p>Provides simple means to get all the current operating systems, as well as as registering for
 * operating system lifecycle (added and removed) events.
 *
 * <p>Provides the ability to start monitoring statistics in a polling fashion. Once monitoring is performed,
 * a statistics event listener can be registered in order to receive statistics change events. Accessing the
 * statistics (without actively monitoring them) is also possible using the {@link #getStatistics()}.
 *
 * <p>When starting to monitor for statistics, each {@link org.openspaces.admin.os.OperatingSystem} will also
 * start its statistics monitoring, and newly discovered ones will also start monitoring for statistics automatically.
 *
 * @author kimchy
 */
public interface OperatingSystems extends Iterable<OperatingSystem>, AdminAware, StatisticsMonitor {

    /**
     * Returns all currently discovered operating systems.
     */
    OperatingSystem[] getOperatingSystems();

    /**
     * Returns the operating system matching its uid.
     */
    OperatingSystem getByUID(String uid);

    /**
     * Returns a map of operating systems with the key as the uid.
     */
    Map<String, OperatingSystem> getUids();

    /**
     * Returns the number of operating systems current discovered.
     */
    int getSize();

    /**
     * Returns an aggregated view of all the operating systems details.
     */
    OperatingSystemsDetails getDetails();

    /**
     * Returns an aggregated view of all the operating systems statistics.
     */
    OperatingSystemsStatistics getStatistics();

    /**
     * Allows to register {@link org.openspaces.admin.os.events.OperatingSystemsStatisticsChangedEventListener}
     * to be notified when statistics have changed. Note, statistics monitoring need to be started using
     * {@link #startStatisticsMonitor()} in order to receive events.
     */
    OperatingSystemsStatisticsChangedEventManager getStatisticsChanged();

    /**
     * Allows to register {@link org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventListener}
     * to be notified when statistics have changed. Note, statistics monitoring need to be started using
     * {@link #startStatisticsMonitor()} in order to receive events.
     */
    OperatingSystemStatisticsChangedEventManager getOperatingSystemStatisticsChanged();
}
