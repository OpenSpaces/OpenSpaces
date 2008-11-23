package org.openspaces.admin.internal.admin;

import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import org.openspaces.admin.LookupService;

/**
 * @author kimchy
 */
public interface InternalLookupService extends LookupService {

    ServiceID getServiceID();

    ServiceRegistrar getRegistrar();

    void setMachine(InternalMachine machine);
}
