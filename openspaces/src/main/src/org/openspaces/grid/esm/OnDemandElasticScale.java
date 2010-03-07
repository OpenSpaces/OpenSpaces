package org.openspaces.grid.esm;

import org.openspaces.admin.machine.Machine;

/**
 * An API to be called upon when in need to scale out to a different machine, or scale in when the machine is no longer needed.
 * When scaling
 */
public interface OnDemandElasticScale {
    
    /**
     * An initialization call, parameterized with the configuration used at deployment.
     * @param config The scale configuration.
     */
    public void init(ElasticScaleConfig config);
    
    /**
     * A machine can be accepted/not-accepted (filtered) for whatever reason at any point in time.
     * This will prevent from a GSC to be started on it.
     * 
     * @param machine The machine to accept to filter.
     * @return <code>true</code> to use this machine to start a GSC on; <code>false</code> to skip it.
     */
    public boolean accept(Machine machine);
    
    /**
     * A scale out request to start a machine. Implementation may choose to block until it has scaled out,
     * or delegate it to a background thread to be performed asynchronously. As long as the machine has not
     * been discovered, we will nag you with a scale out request. When a new machine is started, it should
     * start a GSA (agent).
     * 
     * @param command Command details
     */
    public void scaleOut(ElasticScaleCommand command);
    
    /**
     * A scale in request to terminate a machine. Implementation may choose to ignore it, or decide to terminate
     * considering cost/benefit factors. Will be called when the last GSC on this machine has been terminated.
     * 
     * @param machine The machine to terminate.
     */
    public void scaleIn(Machine machine);
}
