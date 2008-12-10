package org.openspaces.admin.gsa.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.gsa.GridServiceAgent;

/**
 * @author kimchy
 */
public interface GridServiceAgentAddedEventListener extends AdminEventListener {

    void gridServiceAgentAdded(GridServiceAgent gridServiceAgent);
}