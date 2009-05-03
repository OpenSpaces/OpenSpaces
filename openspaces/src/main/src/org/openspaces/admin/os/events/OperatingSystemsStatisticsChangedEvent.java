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

import org.openspaces.admin.os.OperatingSystems;
import org.openspaces.admin.os.OperatingSystemsStatistics;

/**
 * An event indicating that {@link OperatingSystemsStatistics} have changed. Only raised when statistics
 * monitoring is enabled using {@link org.openspaces.admin.os.OperatingSystems#startStatisticsMonitor()}.
 *
 * @author kimchy
 */
public class OperatingSystemsStatisticsChangedEvent {

    private final OperatingSystems operatingSystems;

    private final OperatingSystemsStatistics statistics;

    public OperatingSystemsStatisticsChangedEvent(OperatingSystems operatingSystems, OperatingSystemsStatistics statistics) {
        this.operatingSystems = operatingSystems;
        this.statistics = statistics;
    }

    /**
     * Returns the operating systems component associated with the event.
     */
    public OperatingSystems getOperatingSystems() {
        return operatingSystems;
    }

    /**
     * Returns the statistics associated with the event.
     */
    public OperatingSystemsStatistics getStatistics() {
        return statistics;
    }
}