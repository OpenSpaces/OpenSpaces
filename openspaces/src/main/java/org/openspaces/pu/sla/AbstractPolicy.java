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

package org.openspaces.pu.sla;

/**
 * Simple base class for different policies.
 *
 * @author kimchy
 */
public abstract class AbstractPolicy implements Policy {

    private static final long serialVersionUID = 3791695622307938554L;

    private String monitor;

    private double low;

    private double high;

    private long lowerDampener = 3000;

    private long upperDampener = 3000;

    /**
     * @see Policy#getHigh()
     */
    public double getHigh() {
        return high;
    }

    /**
     * @see Policy#setHigh(double)
     */
    public void setHigh(double high) {
        this.high = high;
    }

    /**
     * @see Policy#getLow()
     */
    public double getLow() {
        return low;
    }

    /**
     * @see Policy#setLow(double)
     */
    public void setLow(double low) {
        this.low = low;
    }

    /**
     * @see Policy#getMonitor()
     */
    public String getMonitor() {
        return monitor;
    }

    /**
     * @see Policy#setMonitor(String)
     */
    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }

    /**
     * Returns the lower dampener for this policy. If the lower
     */
    public long getLowerDampener() {
        return lowerDampener;
    }

    /**
     * Sets the lower dampener for this policy.
     */
    public void setLowerDampener(long lowerDampener) {
        this.lowerDampener = lowerDampener;
    }

    /**
     * Returns the lower dampener for this policy.
     */
    public long getUpperDampener() {
        return upperDampener;
    }

    public void setUpperDampener(long upperDampener) {
        this.upperDampener = upperDampener;
    }
}
