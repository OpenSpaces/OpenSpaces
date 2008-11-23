package org.openspaces.admin.internal.admin;

import org.openspaces.admin.Admin;

/**
 * @author kimchy
 */
public interface InternalAdmin extends Admin {

    void addLookupService(InternalLookupService lookupService);

    void removeLookupService(String uid);
}
