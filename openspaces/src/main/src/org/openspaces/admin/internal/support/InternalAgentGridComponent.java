package org.openspaces.admin.internal.support;

import org.openspaces.admin.AgentGridComponent;
import org.openspaces.admin.gsa.GridServiceAgent;

/**
 * @author kimchy
 */
public interface InternalAgentGridComponent extends AgentGridComponent, InternalGridComponent {

    String getAgentUid();

    void setGridServiceAgent(GridServiceAgent gridServiceAgent);
}
