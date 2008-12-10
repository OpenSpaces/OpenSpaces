package org.openspaces.admin;

import org.openspaces.admin.gsa.GridServiceAgent;

/**
 * @author kimchy
 */
public interface AgentGridComponent extends GridComponent {

    GridServiceAgent getGridServiceAgent();

    void kill();

    void restart();
}
