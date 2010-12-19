package org.openspaces.admin.internal.gsa;

import net.jini.core.lookup.ServiceID;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.esm.ProcessingUnitElasticConfig.GridServiceContainerConfig;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.internal.support.InternalGridComponent;

import com.gigaspaces.grid.gsa.AgentProcessesDetails;
import com.gigaspaces.grid.gsa.GSA;

/**
 * @author kimchy
 */
public interface InternalGridServiceAgent extends GridServiceAgent, InternalGridComponent {

    ServiceID getServiceID();

    GSA getGSA();

    void setProcessesDetails(AgentProcessesDetails processesDetails);

    void kill(InternalAgentGridComponent agentGridComponent);

    void restart(InternalAgentGridComponent agentGridComponent);

    int internalStartGridService(GridServiceContainerConfig config);
}
