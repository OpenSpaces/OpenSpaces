package org.openspaces.admin.internal.admin;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.lus.InternalLookupService;

/**
 * @author kimchy
 */
public interface InternalAdmin extends Admin {

    void addLookupService(InternalLookupService lookupService);

    void removeLookupService(String uid);

    void addGridServiceManager(InternalGridServiceManager gridServiceManager);

    void removeGridServiceManager(String uid);

    void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer);

    void removeGridServiceContainer(String uid);
}
