package org.openspaces.admin.machine;

import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.transport.Transports;

/**
 * @author kimchy
 */
public interface Machine {

    String getUID();

    String getHost();

    LookupServices getLookupServices();

    GridServiceManagers getGridServiceManagers();

    GridServiceContainers getGridServiceContainers();

    boolean hasGridComponents();

    Transports getTransports();
}
