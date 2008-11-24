package org.openspaces.admin.internal.admin.machine;

import org.openspaces.admin.GridServiceContainers;
import org.openspaces.admin.GridServiceManagers;
import org.openspaces.admin.LookupServices;
import org.openspaces.admin.Transports;
import org.openspaces.admin.internal.admin.gsc.DefaultGridServiceContainers;
import org.openspaces.admin.internal.admin.gsc.InternalGridServiceContainers;
import org.openspaces.admin.internal.admin.gsm.DefaultGridServiceManagers;
import org.openspaces.admin.internal.admin.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.admin.lus.DefaultLookupServices;
import org.openspaces.admin.internal.admin.lus.InternalLookupServices;
import org.openspaces.admin.internal.admin.transport.DefaultTransports;
import org.openspaces.admin.internal.admin.transport.InternalTransports;

/**
 * @author kimchy
 */
public class DefaultMachine implements InternalMachine {

    private String uid;

    private String host;

    private final InternalLookupServices lookupServices = new DefaultLookupServices();

    private final InternalGridServiceManagers gridServiceManagers = new DefaultGridServiceManagers();

    private final InternalGridServiceContainers gridServiceContainers = new DefaultGridServiceContainers();

    private final InternalTransports transports = new DefaultTransports();

    public DefaultMachine(String uid, String host) {
        this.uid = uid;
        this.host = host;
    }

    public String getUID() {
        return this.uid;
    }

    public String getHost() {
        return this.host;
    }

    public LookupServices getLookupServices() {
        return lookupServices;
    }

    public GridServiceManagers getGridServiceManagers() {
        return gridServiceManagers;
    }

    public GridServiceContainers getGridServiceContainers() {
        return gridServiceContainers;
    }

    public Transports getTransports() {
        return transports;
    }
}
