package org.openspaces.admin.agent;

import org.openspaces.admin.GridComponent;

/**
 * @author kimchy
 */
public interface GridServiceAgent extends GridComponent {

    void startGridServiceManager();
}
