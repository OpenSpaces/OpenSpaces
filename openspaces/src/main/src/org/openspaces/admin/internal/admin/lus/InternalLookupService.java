package org.openspaces.admin.internal.admin.lus;

import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import org.openspaces.admin.LookupService;
import org.openspaces.admin.internal.admin.machine.InternalMachineAware;
import org.openspaces.admin.internal.admin.transport.InternalTransportAware;
import org.openspaces.admin.internal.admin.transport.InternalTransportInfoProvider;

/**
 * @author kimchy
 */
public interface InternalLookupService extends LookupService, InternalTransportInfoProvider, InternalTransportAware, InternalMachineAware {

    ServiceID getServiceID();

    ServiceRegistrar getRegistrar();
}
