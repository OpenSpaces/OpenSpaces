package org.openspaces.admin.internal.lus;

import org.openspaces.admin.lus.LookupServices;

/**
 * @author kimchy
 */
public interface InternalLookupServices extends LookupServices {

    void addLookupService(InternalLookupService lookupService);

    InternalLookupService removeLookupService(String UID);
}