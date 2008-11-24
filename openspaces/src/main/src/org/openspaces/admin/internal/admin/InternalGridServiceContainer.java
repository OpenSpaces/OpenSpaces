package org.openspaces.admin.internal.admin;

import com.gigaspaces.grid.gsc.GSC;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.GridServiceContainer;

/**
 * @author kimchy
 */
public interface InternalGridServiceContainer extends GridServiceContainer {

    ServiceID getServiceID();

    GSC getGSC();

    void setMachine(InternalMachine machine);
}