package org.openspaces.grid.gsm.containers;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.core.PollingFuture;

public interface FutureGridServiceContainer extends PollingFuture<GridServiceContainer> {

    /**
     * @return the configuration used to start the container
     */
    GridServiceContainerConfig getGridServiceContainerConfig();
    
    /**
     * @return the agent that starts the container
     */
    GridServiceAgent getGridServiceAgent();
   
}
