package org.openspaces.grid.esm;

import org.openspaces.admin.machine.Machine;

public class NullOnDemandElasticScale implements ElasticScaleHandler {

    public void init(ElasticScaleConfig config) {
    }

    public boolean accept(Machine machine) {
        return true;
    }
    
    public void scaleOut(ElasticScaleHandlerContext command) {
    }

    public void scaleIn(Machine machine) {
    }
}
