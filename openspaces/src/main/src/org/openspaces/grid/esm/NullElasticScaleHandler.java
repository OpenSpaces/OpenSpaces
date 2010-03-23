package org.openspaces.grid.esm;

import java.util.logging.Logger;

import org.openspaces.admin.machine.Machine;

public class NullElasticScaleHandler implements ElasticScaleHandler {

    private final static Logger logger = Logger.getLogger("org.openspaces.grid.esm");
    
    public void init(ElasticScaleConfig config) {
    }

    public boolean accept(Machine machine) {
        return true;
    }
    
    public void scaleOut(ElasticScaleHandlerContext context) {
        logger.info("Request to scale out - Needs a new machine");
    }

    public void scaleIn(Machine machine) {
        logger.info("Request to scale in - No need for this machine [" + ToStringHelper.machineToString(machine)+"]");
    }
}
