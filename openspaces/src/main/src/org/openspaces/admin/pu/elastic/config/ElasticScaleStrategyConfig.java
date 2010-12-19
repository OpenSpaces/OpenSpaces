package org.openspaces.admin.pu.elastic.config;

import org.openspaces.admin.bean.BeanConfig;

public interface ElasticScaleStrategyConfig extends BeanConfig {

    int getPollingIntervalSeconds();
    
    /**
     * Defines the polling interval of the cluster state
     * @param pollingIntervalSeconds
     */
    void setPollingIntervalSeconds(int pollingIntervalSeconds);

}
