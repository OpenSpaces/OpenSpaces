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

package org.openspaces.admin.vm.events;

import org.openspaces.admin.vm.VirtualMachines;
import org.openspaces.admin.vm.VirtualMachinesStatistics;

/**
 * An event indicating that an aggregated virtual machines level statistics has changed.
 *
 * <p>Note, monitoring needs to be enabled in order to receive the events.
 *
 * @author kimchy
 * @see VirtualMachinesStatisticsChangedEventListener
 * @see VirtualMachinesStatisticsChangedEventManager
 */
public class VirtualMachinesStatisticsChangedEvent {

    private final VirtualMachines virtualMachines;

    private final VirtualMachinesStatistics statistics;

    public VirtualMachinesStatisticsChangedEvent(VirtualMachines virtualMachines, VirtualMachinesStatistics statistics) {
        this.virtualMachines = virtualMachines;
        this.statistics = statistics;
    }

    /**
     * Returns the virtual machines associated with this events.
     */
    public VirtualMachines getVirtualMachines() {
        return virtualMachines;
    }

    /**
     * Returns the aggregated virtual machines statistics sampled.
     */
    public VirtualMachinesStatistics getStatistics() {
        return statistics;
    }
}