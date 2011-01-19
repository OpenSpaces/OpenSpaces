package org.openspaces.admin.pu.elastic.config;

import org.openspaces.admin.bean.BeanConfig;

public interface ScaleStrategyBeanConfigurer<T extends BeanConfig> {

    T getConfig();
}
