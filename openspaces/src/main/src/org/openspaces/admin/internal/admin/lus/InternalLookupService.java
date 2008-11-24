package org.openspaces.admin.internal.admin.lus;

import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import org.openspaces.admin.LookupService;
import org.openspaces.admin.internal.admin.machine.InternalMachine;

/**
 * @author kimchy
 */
public interface InternalLookupService extends LookupService {

    ServiceID getServiceID();

    ServiceRegistrar getRegistrar();

    void setMachine(InternalMachine machine);
}
