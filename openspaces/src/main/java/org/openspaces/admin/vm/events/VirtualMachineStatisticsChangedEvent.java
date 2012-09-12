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

import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachineStatistics;

/**
 * An event indicating that a virtual machine level statistics has changed.
 *
 * <p>Note, monitoring needs to be enabled in order to receive the events.
 *
 * @author kimchy
 * @see VirtualMachineStatisticsChangedEventListener
 * @see VirtualMachineStatisticsChangedEventManager
 */
public class VirtualMachineStatisticsChangedEvent {

    private final VirtualMachine virtualMachine;

    private final VirtualMachineStatistics statistics;

    public VirtualMachineStatisticsChangedEvent(VirtualMachine virtualMachine, VirtualMachineStatistics statistics) {
        this.virtualMachine = virtualMachine;
        this.statistics = statistics;
    }

    /**
     * Returns the virtual machine associated with this event.
     */
    public VirtualMachine getVirtualMachine() {
        return virtualMachine;
    }

    /**
     * Returns the statistics of the virtual machine sampled.
     */
    public VirtualMachineStatistics getStatistics() {
        return statistics;
    }
}