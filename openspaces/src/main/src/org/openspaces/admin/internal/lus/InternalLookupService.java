package org.openspaces.admin.internal.lus;

import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import org.openspaces.admin.internal.machine.InternalMachineAware;
import org.openspaces.admin.internal.transport.InternalTransportAware;
import org.openspaces.admin.internal.transport.InternalTransportInfoProvider;
import org.openspaces.admin.lus.LookupService;

/**
 * @author kimchy
 */
public interface InternalLookupService extends LookupService, InternalTransportInfoProvider, InternalTransportAware, InternalMachineAware {

    ServiceID getServiceID();

    ServiceRegistrar getRegistrar();
}
