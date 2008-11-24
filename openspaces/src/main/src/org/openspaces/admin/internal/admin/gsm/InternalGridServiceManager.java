package org.openspaces.admin.internal.admin.gsm;

import com.gigaspaces.grid.gsm.GSM;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.GridServiceManager;
import org.openspaces.admin.internal.admin.machine.InternalMachineAware;
import org.openspaces.admin.internal.admin.transport.InternalTransportAware;
import org.openspaces.admin.internal.admin.transport.InternalTransportInfoProvider;

/**
 * @author kimchy
 */
public interface InternalGridServiceManager extends GridServiceManager, InternalTransportInfoProvider, InternalTransportAware, InternalMachineAware {

    ServiceID getServiceID();

    GSM getGSM();
}