package org.openspaces.admin.gsa;

import org.openspaces.admin.GridComponent;

/**
 * @author kimchy
 */
public interface GridServiceAgent extends GridComponent {

    void startGridServiceManager();

    void startGridServiceContainer();

    void startLookupService();
}
