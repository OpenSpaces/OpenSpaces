package org.openspaces.admin.internal.support;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;

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
        assertStateChangesPermitted();
        this.gridServiceAgent = gridServiceAgent;
    }

    public void kill() {
        if (gridServiceAgent == null) {
            throw new AdminException("Not associated with an agent to perform kill operation");
        }
        ((InternalGridServiceAgent) gridServiceAgent).kill(this);
    }

    public void restart() {
        if (gridServiceAgent == null) {
            throw new AdminException("Not associated with an agent to perform restart operation");
        }
        ((InternalGridServiceAgent) gridServiceAgent).restart(this);
    }
}
