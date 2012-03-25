package org.openspaces.admin.pu.statistics;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;

/**
 * Picks the minimum of all cluster instances values. 
 * @since 9.0.0
 * @author itaif
 *
 */
public class MinimumInstancesStatisticsConfig 
            extends AbstractInstancesStatisticsConfig 
            implements StatisticsObjectListFunction , InstancesStatisticsConfig {
    
    public MinimumInstancesStatisticsConfig() {
        this(new HashMap<String,String>());
    }
    
    public MinimumInstancesStatisticsConfig(Map<String,String> properties) {
        super(properties);
    }
    
    @Override
    public void validate() throws IllegalStateException {
        //ok
    }

    @Override
    public Object calc(StatisticsObjectList values) {
        return values.getMinimum();
    }
    
}