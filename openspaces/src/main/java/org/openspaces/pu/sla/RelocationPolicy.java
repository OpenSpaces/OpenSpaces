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
 * Relocation policy will cause a processing unit instance to relocate when the policy
 * associated monitor breaches its threshold values. Relocation means that
 * the processing unit will be removed from its current grid container and
 * moved to a new one (that meets its requirements).
 *
 * @author kimchy
 */
public class RelocationPolicy extends AbstractPolicy {

    private static final long serialVersionUID = 4958476938556141813L;

    public String toString() {
        return "RelocationPolicy monitor [" + getMonitor() + "] low [" + getLow() + "] high [" + getHigh() + "]";
    }
}
