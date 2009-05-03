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

package org.openspaces.admin.os.events;

import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.os.OperatingSystemStatistics;

/**
 * An event indicating that {@link OperatingSystemStatistics} have changed. Only raised when statistics
 * monitoring is enabled using {@link org.openspaces.admin.os.OperatingSystem#startStatisticsMonitor()}.
 *
 * @author kimchy
 */
public class OperatingSystemStatisticsChangedEvent {

    private final OperatingSystem operatingSystem;

    private final OperatingSystemStatistics statistics;

    public OperatingSystemStatisticsChangedEvent(OperatingSystem operatingSystem, OperatingSystemStatistics statistics) {
        this.operatingSystem = operatingSystem;
        this.statistics = statistics;
    }

    /**
     * Returns the operating system associated with the event.
     */
    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * Returns the operating system statistics associated with the event.
     */
    public OperatingSystemStatistics getStatistics() {
        return statistics;
    }
}