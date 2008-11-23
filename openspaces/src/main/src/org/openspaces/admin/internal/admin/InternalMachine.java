package org.openspaces.admin.internal.admin;

import org.openspaces.admin.Machine;

/**
 * @author kimchy
 */
public interface InternalMachine extends Machine {

    void addLookupService(InternalLookupService lookupService);

    void removeLookupService(String uid);
}
