package org.openspaces.admin.internal.admin.machine;

import org.openspaces.admin.GridServiceContainers;
import org.openspaces.admin.GridServiceManagers;
import org.openspaces.admin.LookupServices;
import org.openspaces.admin.internal.admin.gsc.DefaultGridServiceContainers;
import org.openspaces.admin.internal.admin.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.admin.gsc.InternalGridServiceContainers;
import org.openspaces.admin.internal.admin.gsm.DefaultGridServiceManagers;
import org.openspaces.admin.internal.admin.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.admin.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.admin.lus.DefaultLookupServices;
import org.openspaces.admin.internal.admin.lus.InternalLookupService;
import org.openspaces.admin.internal.admin.lus.InternalLookupServices;

/**
 * @author kimchy
 */
public class DefaultMachine implements InternalMachine {

    private String uid;

    private String host;

    private final InternalLookupServices lookupServices = new DefaultLookupServices();

    private final InternalGridServiceManagers gridServiceManagers = new DefaultGridServiceManagers();

    private final InternalGridServiceContainers gridServiceContainers = new DefaultGridServiceContainers();

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

    public void addLookupService(InternalLookupService lookupService) {
        lookupServices.addLookupService(lookupService);
    }

    public void removeLookupService(String uid) {
        lookupServices.removeLookupService(uid);
    }

    public GridServiceManagers getGridServiceManagers() {
        return gridServiceManagers;
    }

    public GridServiceContainers getGridServiceContainers() {
        return gridServiceContainers;
    }

    public void addGridServiceManager(InternalGridServiceManager gridServiceManager) {
        gridServiceManagers.addGridServiceManager(gridServiceManager);
    }

    public void removeGridServiceManager(String uid) {
        gridServiceManagers.removeGridServiceManager(uid);
    }

    public void replaceGridServiceManager(InternalGridServiceManager gridServiceManager) {
        gridServiceManagers.replaceGridServiceManager(gridServiceManager);
    }

    public void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer) {
        gridServiceContainers.addGridServiceContainer(gridServiceContainer);
    }

    public void removeGridServiceContainer(String uid) {
        gridServiceContainers.removeGridServiceContainer(uid);
    }

    public void replaceGridServiceContainer(InternalGridServiceContainer gridServiceContainer) {
        gridServiceContainers.replaceGridServiceContainer(gridServiceContainer);
    }
}
