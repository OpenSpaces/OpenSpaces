package org.openspaces.admin.internal.alerts.bean.util;

import java.util.List;

public class AlertBeanUtils {
    
    /**
     * average in specified period.
     * @param period sliding window of timeline samples.
     * @param timeline timeline of samples.
     * @return -1 if not enough samples; average of samples within period.
     */
    public static double getAverage(int period, List<Double> timeline) {
        if (period > timeline.size()) return -1;

        double average = 0.0;
        for (int i = 0; i < period && i < timeline.size(); i++) {
            double value = timeline.get(i);
            average += value;
        }
        average /= period;

        return average;
    }
}
