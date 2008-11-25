package org.openspaces.admin.internal.gsc;

import com.gigaspaces.grid.gsc.GSC;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.machine.InternalMachineAware;
import org.openspaces.admin.internal.os.InternalOperatingSystemInfoProvider;
import org.openspaces.admin.internal.transport.InternalTransportAware;
import org.openspaces.admin.internal.transport.InternalTransportInfoProvider;
import org.openspaces.admin.internal.vm.InternalVirtualMachineInfoProvider;

/**
 * @author kimchy
 */
public interface InternalGridServiceContainer extends GridServiceContainer, InternalTransportInfoProvider, InternalTransportAware,
        InternalMachineAware, InternalOperatingSystemInfoProvider, InternalVirtualMachineInfoProvider {

    ServiceID getServiceID();

    GSC getGSC();
}