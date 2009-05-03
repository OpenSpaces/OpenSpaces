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

/**
 * An operating system is a virtual entity that container information about the operating system one
 * or more grid components are running within. An operating system is bounded to a {@link org.openspaces.admin.machine.Machine}
 * and shares the same lifecycle.
 *
 * @author kimchy
 */
public interface OperatingSystem extends AdminAware, StatisticsMonitor {

    /**
     * Returns the uid of the operating system.
     */
    String getUid();

    /**
     * Returns details (static view) of the operating system.
     */
    OperatingSystemDetails getDetails();

    /**
     * Returns the statistics of the operating system.
     */
    OperatingSystemStatistics getStatistics();

    /**
     * Allows to register for {@link org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventListener}s.
     *
     * <p>Note, in order to receive events, the {@link #startStatisticsMonitor()} needs to be called.
     */
    OperatingSystemStatisticsChangedEventManager getStatisticsChanged();
}
