package org.openspaces.admin.pu.elastic.config;

public interface ScaleBeanConfigurer<T extends ElasticScaleStrategyConfig> {

    T getConfig();
}
