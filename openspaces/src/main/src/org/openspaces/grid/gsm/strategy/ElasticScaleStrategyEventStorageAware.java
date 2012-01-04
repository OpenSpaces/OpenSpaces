package org.openspaces.grid.gsm.strategy;

import org.jini.rio.monitor.event.EventsStore;

public interface ElasticScaleStrategyEventStorageAware {
    
    void setElasticScaleStrategyEventStorage(EventsStore eventsStore);
}
