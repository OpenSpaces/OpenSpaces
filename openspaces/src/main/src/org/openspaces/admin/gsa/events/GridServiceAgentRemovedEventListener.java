package org.openspaces.admin.gsa.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.gsa.GridServiceAgent;

/**
 * @author kimchy
 */
public interface GridServiceAgentRemovedEventListener extends AdminEventListener {

    void gridServiceAgentRemoved(GridServiceAgent gridServiceAgent);
}