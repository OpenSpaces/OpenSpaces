package org.openspaces.grid.esm;

import org.openspaces.admin.machine.Machine;

/**
 * An API to be called upon when in need to scale out to a different machine, or scale in when the machine is no longer needed.
 * When scaling
 * 
 * <blockquote>
 * <pre>
 * <b>Disclaimer:</b> This interface and the elastic data grid functionality is provided as a technology preview in XAP 7.1. 
 * As such, it is subject to API and behavior changes in the next XAP releases without going the usual deprecation process 
 * of the XAP API.
 * </pre>
 * </blockquote>
 */
public interface ElasticScaleHandler {
    
    /**
     * An initialization call, parameterized with the configuration used at deployment.
     * @param config The scale configuration.
     */
    public void init(ElasticScaleHandlerConfig config);
    
    /**
     * A machine can be accepted/not-accepted (filtered) for whatever reason at any point in time.
     * This will prevent from a GSC to be started on it.
     * 
     * @param machine The machine to accept to filter.
     * @return <code>true</code> to use this machine to start a GSC on; <code>false</code> to skip it.
     */
    public boolean accept(Machine machine);

    /**
     * A scale out request to start a machine. Implementation may choose to block until it has
     * scaled out (which will cause the ESM to wait until the call has returned, preventing it from
     * handling other processing units). It's advised to scale out asynchronously due to long
     * startup delays. As long as the machine has not been discovered, the ESM will repeatedly call
     * this method until a new machine has been allocated. It’s up to the implementation class to
     * maintain the state and not allocate new resources before the pending allocations have
     * completed. When a new machine is started, it should start a Grid Service Agent (GSA).
     * 
     * @param context
     *            Context details
     */
    public void scaleOut(ElasticScaleHandlerContext context);
    
    /**
     * A scale in request to terminate a machine. Implementation may choose to ignore it, or decide to terminate
     * considering cost/benefit factors. Will be called only once per-last GSC on this machine to terminate. Implementation
     * may choose to block until it has scaled in (which will cause the ESM to wait until the call has returned,
     * preventing it from handling other processing units).
     * 
     * @param machine The machine to terminate.
     */
    public void scaleIn(Machine machine);
}
