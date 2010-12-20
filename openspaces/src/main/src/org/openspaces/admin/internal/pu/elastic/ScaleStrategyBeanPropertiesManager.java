package org.openspaces.admin.internal.pu.elastic;

import java.util.Map;

public class ScaleStrategyBeanPropertiesManager extends FlattenedBeanConfigPropertiesManager {
    private static final String ELASTIC_SCALE_STRATEGY_CLASSNAMES_KEY = "elastic-scale-strategy-classnames";
    private static final String ELASTIC_SCALE_STRATEGY_ENABLED_CLASSNAME_KEY = "elastic-scale-strategy-enabled-classname";
    
    public ScaleStrategyBeanPropertiesManager(
            Map<String, String> properties) {
        super(ELASTIC_SCALE_STRATEGY_CLASSNAMES_KEY, ELASTIC_SCALE_STRATEGY_ENABLED_CLASSNAME_KEY, properties);
    }
}
