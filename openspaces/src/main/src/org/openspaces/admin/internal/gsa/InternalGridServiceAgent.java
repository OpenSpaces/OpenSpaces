package org.openspaces.admin.internal.gsa;

import com.gigaspaces.grid.gsa.GSA;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.internal.support.InternalGridComponent;

/**
 * @author kimchy
 */
public interface InternalGridServiceAgent extends GridServiceAgent, InternalGridComponent {

    ServiceID getServiceID();

    GSA getGSA();

    void kill(InternalAgentGridComponent agentGridComponent);

    void restart(InternalAgentGridComponent agentGridComponent);
}
