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

import java.io.Serializable;

/**
 * A monitor bean. Used to monitor different beans within a processing unit.
 *
 * @author kimchy
 */
public interface Monitor extends Serializable {

    /**
     * Returns the name of the monitor. Used in different UI handlers and
     * SLA references.
     */
    String getName();

    /**
     * A period (in <b>milliseconds</b>) when this value will be checked.
     */
    long getPeriod();

    /**
     * The value of the monitor.
     */
    double getValue();

    /**
     * The size of the history of values saved.
     */
    int getHistorySize();
}
