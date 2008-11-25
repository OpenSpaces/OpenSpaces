package org.openspaces.admin.internal.gsm;

import com.gigaspaces.grid.gsm.GSM;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.machine.InternalMachineAware;
import org.openspaces.admin.internal.os.InternalOperatingSystemInfoProvider;
import org.openspaces.admin.internal.transport.InternalTransportAware;
import org.openspaces.admin.internal.transport.InternalTransportInfoProvider;
import org.openspaces.admin.internal.vm.InternalVirtualMachineInfoProvider;

/**
 * @author kimchy
 */
public interface InternalGridServiceManager extends GridServiceManager, InternalTransportInfoProvider, InternalTransportAware,
        InternalMachineAware, InternalOperatingSystemInfoProvider, InternalVirtualMachineInfoProvider {

    ServiceID getServiceID();

    GSM getGSM();
}