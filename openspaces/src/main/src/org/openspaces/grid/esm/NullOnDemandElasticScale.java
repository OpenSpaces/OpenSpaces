package org.openspaces.grid.esm;

import org.openspaces.admin.machine.Machine;

public class NullOnDemandElasticScale implements OnDemandElasticScale {

    public void init(ElasticScaleConfig config) {
    }

    public boolean accept(Machine machine) {
        return true;
    }
    
    public void scaleOut(ElasticScaleCommand command) {
    }

    public void scaleIn(Machine machine) {
    }
}
