package org.openspaces.admin.pu.statistics;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;


public class AverageTimeWindowStatisticsConfig 
            extends AbstractTimeWindowStatisticsConfig 
            implements StatisticsObjectListFunction {

    public AverageTimeWindowStatisticsConfig() {
        this(new HashMap<String,String>());
    }
    
    public AverageTimeWindowStatisticsConfig(Map<String, String> properties) {
        super(properties);
    }


    @Override
    public Object calc(StatisticsObjectList values) {
        return values.getAverage();
    }
}
