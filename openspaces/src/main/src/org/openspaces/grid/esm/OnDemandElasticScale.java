package org.openspaces.grid.esm;

import org.openspaces.admin.machine.Machine;

public interface OnDemandElasticScale {
    
    public void init(ElasticScaleConfig config);
    
    public boolean accept(Machine machine);
    
    public void scaleOut(ElasticScaleCommand command);
}
