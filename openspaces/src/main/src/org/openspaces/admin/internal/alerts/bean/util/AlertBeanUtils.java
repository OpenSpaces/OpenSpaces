package org.openspaces.admin.internal.alerts.bean.util;

import java.util.List;
import java.util.UUID;

import org.openspaces.admin.internal.alerts.bean.AlertBean;

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

    /**
     * Generate a unique bean UUID for a specific class by it's name.
     * 
     * @param clazz
     *            the class of the alert bean to generate a UUID for.
     * @return a UUID consisting of the name as hexadecimal digits concatenated with a random UUID.
     */
    public static String generateBeanUUID(Class<? extends AlertBean> clazz) {
        return Integer.toHexString(clazz.getSimpleName().hashCode())+"-"+UUID.randomUUID();
    }
}
