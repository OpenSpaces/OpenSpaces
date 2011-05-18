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

package org.openspaces.pu.sla.monitor;

/**
 * A base class for monitor classes.
 *
 * @author kimchy
 */
public abstract class AbstractMonitor implements Monitor {

    private static final long serialVersionUID = 6622476878904436821L;

    private String name;

    private long period = 5000;

    private int historySize = 100;

    /**
     * Returns the name of the monitor.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the monitor.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The period this monitor will be sampled (in milliseconds). Defaults to 5 seconds.
     */
    public long getPeriod() {
        return period;
    }

    /**
     * The period this monitor will be sampled (in milliseconds). Defaults to 5 seconds.
     */
    public void setPeriod(long period) {
        this.period = period;
    }

    /**
     * The history log size that will be kept for this monitor. Defaults to 100.
     */
    public int getHistorySize() {
        return historySize;
    }

    /**
     * The history log size that will be kept for this monitor. Defaults to 100.
     */
    public void setHistorySize(int historySize) {
        this.historySize = historySize;
    }
}
