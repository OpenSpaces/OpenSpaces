package org.openspaces.admin.transport;

import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEventManager;
import org.openspaces.admin.vm.VirtualMachineAware;

/**
 * @author kimchy
 */
public interface Transport extends VirtualMachineAware, StatisticsMonitor {

    String getUid();

    String getLocalHostAddress();

    String getLocalHostName();

    String getHost();

    int getPort();

    TransportDetails getDetails();

    TransportStatistics getStatistics();

    TransportStatisticsChangedEventManager getStatisticsChanged();
}
