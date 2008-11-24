package org.openspaces.admin.internal.admin;

import org.openspaces.admin.Admin;

/**
 * @author kimchy
 */
public interface InternalAdmin extends Admin {

    void addLookupService(InternalLookupService lookupService);

    void removeLookupService(String uid);

    void addGridServiceManager(InternalGridServiceManager gridServiceManager);

    void removeGridServiceManager(String uid);

    void replaceGridServiceManager(InternalGridServiceManager gridServiceManager);

    void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer);

    void removeGridServiceContainer(String uid);

    void repalceGridServiceContainer(InternalGridServiceContainer gridServiceContainer);
}
