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
 * Scale up policy will cause a processing unit instance to be created
 * when the policy associated monitor breaches its threshold values.
 *
 * @author kimchy
 */
public class ScaleUpPolicy extends AbstractPolicy {

    private static final long serialVersionUID = 4455422998317289836L;

    private int maxInstances;

    /**
     * The maximum number of processing instances this scale up policy will scale
     * up to. Should be higher than {@link #getHigh() high} value.
     */
    public int getMaxInstances() {
        return maxInstances;
    }

    /**
     * The maximum number of processing instances this scale up policy will scale
     * up to. Should be higher than {@link #getHigh() high} value.
     */
    public void setMaxInstances(int maxInstances) {
        this.maxInstances = maxInstances;
    }

    public String toString() {
        return "ScaleUpPolicy monitor [" + getMonitor() + "] low [" + getLow() + "] high [" + getHigh()
                + "] maxInstances [" + getMaxInstances() + "]";
    }
}
