package org.openspaces.grid.gsm.strategy;

import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.core.bean.Bean;

public interface ScaleStrategyBean extends Bean {

    ScaleStrategyConfig getConfig();
    
    /**
     * @return true if the SLA is breached and corrective actions are in progress.
     */
    boolean isScaleInProgress();
}
