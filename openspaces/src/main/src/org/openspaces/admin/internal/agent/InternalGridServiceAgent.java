package org.openspaces.admin.internal.agent;

import com.gigaspaces.grid.gsa.GSA;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.agent.GridServiceAgent;
import org.openspaces.admin.internal.support.InternalGridComponent;

/**
 * @author kimchy
 */
public interface InternalGridServiceAgent extends GridServiceAgent, InternalGridComponent {

    ServiceID getServiceID();

    GSA getGSA();
}
