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

import org.openspaces.admin.machine.MachineAware;
import org.openspaces.admin.os.OperatingSystemAware;
import org.openspaces.admin.transport.TransportAware;
import org.openspaces.admin.vm.VirtualMachineAware;
import org.openspaces.admin.zone.ZoneAware;

/**
 * Grid Component is an element that can provide information on the machine it is running one, the transport
 * it uses, the Operating system it is running on, and the virtual machine that started it.
 *
 * @author kimchy
 */
public interface GridComponent extends DiscoverableComponent, AdminAware, MachineAware, TransportAware, OperatingSystemAware, VirtualMachineAware, ZoneAware {

    /**
     * Returns the unique id of the grid component.
     */
    String getUid();
}
