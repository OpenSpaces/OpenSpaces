package org.openspaces.grid.esm;

import java.util.List;

import org.openspaces.admin.machine.Machine;

/**
 * Elastic scale command passed as a parameter on the call to {@link ElasticScaleHandler#scaleOut(ElasticScaleCommand)}.
 * It holds details such as the set of machines currently discovered.
 * 
 * <blockquote>
 * <pre>
 * <b>Disclaimer:</b> This interface and the elastic data grid functionality is provided as a technology preview in XAP 7.1. 
 * As such, it is subject to API and behavior changes in the next XAP releases without going through the usual deprecation process 
 * of the XAP API.
 * </pre>
 * </blockquote>
 */
public class ElasticScaleHandlerContext {
    private List<Machine> machines;
    
    //package level
    void setMachines(List<Machine> machines) {
        this.machines = machines;
    }
    
    public List<Machine> getMachines() {
        return machines;
    }
    
}
