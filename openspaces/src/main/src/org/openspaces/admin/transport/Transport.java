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

package org.openspaces.admin.transport;

import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEventManager;
import org.openspaces.admin.vm.VirtualMachineAware;

/**
 * A transport holds information on the communication layer that is used by a grid component.
 *
 * @author kimchy
 */
public interface Transport extends VirtualMachineAware, StatisticsMonitor {

    /**
     * Returns the UID of the transport.
     */
    String getUid();

    /**
     * Returns the host address of the transport.
     *
     * @see java.net.InetAddress#getLocalHost()
     * @see java.net.InetAddress#getHostAddress()
     */
    String getHostAddress();

    /**
     * Returns the host address of the transport.
     *
     * @see java.net.InetAddress#getLocalHost()
     * @see java.net.InetAddress#getHostName()
     */
    String getHostName();

    /**
     * Returns the host name or address the communication layer bounded on.
     */
    String getBindHost();

    /**
     * Returns the port number the communication layer is using.
     */
    int getPort();

    /**
     * Returns the details (non changeable) of the transport.
     */
    TransportDetails getDetails();

    /**
     * Returns the transport statistics.
     */
    TransportStatistics getStatistics();

    /**
     * Allows to register for {@link org.openspaces.admin.transport.events.TransportStatisticsChangedEvent}s.
     *
     * <p>Note, the transport needs to be in a monitoring state. See {@link #startStatisticsMonitor()}.
     */
    TransportStatisticsChangedEventManager getStatisticsChanged();
    
    /**
     * Return the LRMI monitoring of this transport.
     * 
     * See {@link TransportLRMIMonitoring}.
     */
    TransportLRMIMonitoring getLRMIMonitoring();
}
