package org.openspaces.admin.internal.support;

import org.openspaces.admin.gsa.GridServiceAgent;

/**
 * @author kimchy
 */
public interface InternalAgentGridComponent extends InternalGridComponent {

    int getAgentId();

    String getAgentUid();

    void setGridServiceAgent(GridServiceAgent gridServiceAgent);
}
