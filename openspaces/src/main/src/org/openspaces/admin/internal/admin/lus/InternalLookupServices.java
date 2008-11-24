package org.openspaces.admin.internal.admin.lus;

import org.openspaces.admin.LookupServices;

/**
 * @author kimchy
 */
public interface InternalLookupServices extends LookupServices {

    void addLookupService(InternalLookupService lookupService);

    InternalLookupService removeLookupService(String UID);
}