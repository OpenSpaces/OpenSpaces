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

import java.io.Serializable;

/**
 * A policy controls the runtime action that should be taken when the
 * monitor value associated with this policy (using {@link #setMonitor(String)})
 * breaches the policy thresholds.
 *
 * <p>The monitor is referenced by name and can use one of the built in monitors
 * that comes with the grid container (<code>CPU</code> and <code>Memory</code>)
 * or one of the custom monitors defined within the {@link SLA}.
 *
 * @author kimchy
 * @see org.openspaces.pu.sla.SLA
 */
public interface Policy extends Serializable {

    /**
     * The monitor name this policy will use in order to get check if its
     * value has breached the policy thresholds ({@link #setHigh(double)} and
     * {@link #setLow(double)}).
     */
    String getMonitor();

    /**
     * The monitor name this policy will use in order to get check if its
     * value has breached the policy thresholds ({@link #setHigh(double)} and
     * {@link #setLow(double)}).
     */
    void setMonitor(String watch);

    /**
     * The high threshold value of the policy.
     */
    double getHigh();

    /**
     * The high threshold value of the policy.
     */
    void setHigh(double high);

    /**
     * The low threshold value of the policy.
     */
    double getLow();

    /**
     * The low threshold value of the policy.
     */
    void setLow(double low);

    /**
     * The lower dampener acts as a time window where if the lower threshold
     * has been cleared (after it has been breached), it won't cause the policy action to happen.
     * Set in <b>milliseconds</b>, defaults to <code>3000</code>.
     */
    long getLowerDampener();

    /**
     * The lower dampener acts as a time window where if the lower threshold
     * has been cleared (after it has been breached), it won't cause the policy action to happen.
     * Set in <b>milliseconds</b>, defaults to <code>3000</code>.
     */
    void setLowerDampener(long lowerDampener);

    /**
     * The upper dampener acts as a time window where if the upper threshold
     * has been cleared (after it has been breached), it won't cause the policy action to happen.
     * Set in <b>milliseconds</b>, defaults to <code>3000</code>.
     */
    long getUpperDampener();

    /**
     * The upper dampener acts as a time window where if the upper threshold
     * has been cleared (after it has been breached), it won't cause the policy action to happen.
     * Set in <b>milliseconds</b>, defaults to <code>3000</code>.
     */
    void setUpperDampener(long upperDampener);
}
