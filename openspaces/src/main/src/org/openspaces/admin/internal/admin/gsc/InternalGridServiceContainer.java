package org.openspaces.admin.internal.admin.gsc;

import com.gigaspaces.grid.gsc.GSC;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.GridServiceContainer;
import org.openspaces.admin.internal.admin.machine.InternalMachine;

/**
 * @author kimchy
 */
public interface InternalGridServiceContainer extends GridServiceContainer {

    ServiceID getServiceID();

    GSC getGSC();

    void setMachine(InternalMachine machine);
}