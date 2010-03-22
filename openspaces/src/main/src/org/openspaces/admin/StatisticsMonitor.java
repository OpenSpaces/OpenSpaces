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

package org.openspaces.admin;

import java.util.concurrent.TimeUnit;

/**
 * Components implement this interface indicating that they can be monitored for statistics.
 *
 * <p>Components will allow to get their respective statistics without being monitoring (while caching
 * the calls for the provided statistics interval).
 *
 * <p>Monitoring statistics is only required when wanting to receive statistics change events.
 *
 * <p>The statistics interval controls either for how long the latest statistics call will be cached, or,
 * when monitoring is enabled, the interval the statistics will be pooled. Its default value is 5 seconds.
 *
 * @author kimchy
 */
public interface StatisticsMonitor {

    /**
     * The default statistics interval which is 5 seconds.
     */
    static final long DEFAULT_MONITOR_INTERVAL = 5000;

    /**
     * The default history size stored in statistics.
     */
    static final int DEFAULT_HISTORY_SIZE = 5000;

    /**
     * Sets the statistics interval, automatically updating the monitoring scheduled tasks if
     * monitoring is enabled.
     */
    void setStatisticsInterval(long interval, TimeUnit timeUnit);

    /**
     * Sets the history size of number of statistics stored.
     */
    void setStatisticsHistorySize(int historySize);

    /**
     * Starts the statistics monitor, starting a scheduled monitor that polls for statistics. Monitoring
     * is required only when wanting to receive statistics change events.
     */
    void startStatisticsMonitor();

    /**
     * Stops the statistics monitor.
     */
    void stopStatisticsMonitor();

    /**
     * Returns <code>true</code> if statistics are now being monitored.
     */
    boolean isMonitoring();
}
