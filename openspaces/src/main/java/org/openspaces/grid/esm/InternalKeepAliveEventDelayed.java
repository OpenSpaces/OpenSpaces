package org.openspaces.grid.esm;

public class InternalKeepAliveEventDelayed extends IllegalStateException {

    private static final long serialVersionUID = 1L;
    
    public InternalKeepAliveEventDelayed(long delaySeconds) {
        super("Single threaded admin keep alive event has been delayed by " + 
                delaySeconds + " seconds.");
    }

}
