package org.openspaces.admin.internal.admin.machine;

import org.openspaces.admin.Machine;
import org.openspaces.admin.internal.admin.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.admin.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.admin.lus.InternalLookupService;

/**
 * @author kimchy
 */
public interface InternalMachine extends Machine {

    void addLookupService(InternalLookupService lookupService);

    void removeLookupService(String uid);

    void addGridServiceManager(InternalGridServiceManager gridServiceManager);

    void removeGridServiceManager(String uid);

    void replaceGridServiceManager(InternalGridServiceManager gridServiceManager);

    void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer);

    void removeGridServiceContainer(String uid);

    void replaceGridServiceContainer(InternalGridServiceContainer gridServiceContainer);
}
