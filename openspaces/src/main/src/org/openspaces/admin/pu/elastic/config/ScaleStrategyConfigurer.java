package org.openspaces.admin.pu.elastic.config;

import org.openspaces.admin.bean.BeanConfig;

public interface ScaleStrategyConfigurer<T extends BeanConfig> {

    T create();
}
