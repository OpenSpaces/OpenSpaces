package org.openspaces.grid.gsm.containers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsc.GridServiceContainer;

public interface FutureGridServiceContainer {

    /**
     * @return the options used to start the container
     */
    GridServiceContainerOptions getGridServiceContainerOptions();
    
    /**
     * @return the agent that starts the container
     */
    GridServiceAgent getGridServiceAgent();
    
    /**
     * @return the grid service container object if started succesfully
     * @throws ExecutionException - if async operation resulted in an exception
     * @throws TimeoutException - if async operation took too much time to complete
     * @throws IllegalStateException - if {@link #isDone()} is false
     */
    GridServiceContainer getGridServiceContainer() throws ExecutionException, IllegalStateException , TimeoutException;
        
    /**
     * @return true if {@link #getGridServiceContainer()} can be called.
     */
    boolean isDone();

    /**
     * 
     * @return the System.currentTimeMillis() when the operation was first submitted
     */
    long getTimestamp();        
   
}
