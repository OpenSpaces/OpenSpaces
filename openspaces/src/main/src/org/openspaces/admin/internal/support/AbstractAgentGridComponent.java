package org.openspaces.admin.internal.support;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.admin.InternalAdmin;

/**
 * @author kimchy
 */
public abstract class AbstractAgentGridComponent extends AbstractGridComponent implements InternalAgentGridComponent {

    private final int agentId;

    private final String agentUid;

    private volatile GridServiceAgent gridServiceAgent;

    public AbstractAgentGridComponent(InternalAdmin admin, int agentId, String agentUid) {
        super(admin);
        this.agentId = agentId;
        this.agentUid = agentUid;
    }

    public int getAgentId() {
        return this.agentId;
    }

    public String getAgentUid() {
        return this.agentUid;
    }

    public GridServiceAgent getGridServiceAgent() {
        return gridServiceAgent;
    }

    public void setGridServiceAgent(GridServiceAgent gridServiceAgent) {
        this.gridServiceAgent = gridServiceAgent;
    }
}
