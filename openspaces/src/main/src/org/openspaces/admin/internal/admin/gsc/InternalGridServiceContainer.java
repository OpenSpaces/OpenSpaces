package org.openspaces.admin.internal.admin.gsc;

import com.gigaspaces.grid.gsc.GSC;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.GridServiceContainer;
import org.openspaces.admin.internal.admin.machine.InternalMachineAware;
import org.openspaces.admin.internal.admin.transport.InternalTransportAware;
import org.openspaces.admin.internal.admin.transport.InternalTransportInfoProvider;

/**
 * @author kimchy
 */
public interface InternalGridServiceContainer extends GridServiceContainer, InternalTransportInfoProvider, InternalTransportAware, InternalMachineAware {

    ServiceID getServiceID();

    GSC getGSC();
}