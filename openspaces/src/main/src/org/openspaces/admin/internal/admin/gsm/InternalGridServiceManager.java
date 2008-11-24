package org.openspaces.admin.internal.admin.gsm;

import com.gigaspaces.grid.gsm.GSM;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.GridServiceManager;
import org.openspaces.admin.internal.admin.machine.InternalMachine;

/**
 * @author kimchy
 */
public interface InternalGridServiceManager extends GridServiceManager {

    ServiceID getServiceID();

    GSM getGSM();

    void setMachine(InternalMachine machine);
}