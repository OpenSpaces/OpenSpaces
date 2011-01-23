package org.openspaces.admin.internal.alert.bean.util;

import java.util.List;
import java.util.UUID;

import org.openspaces.admin.internal.alert.bean.AlertBean;
import org.openspaces.admin.vm.VirtualMachine;

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
    
    /**
     * Returns the short name of a grid component running inside a JVM.
     * Inspects the JVM in the following order - GSM, GSC, GSA, LUS.
     * 
     * @param virtualMachine
     * @return initials of the grid component. empty string if no component found.
     */
    public static String getGridComponentShortName(VirtualMachine virtualMachine) {
        if (virtualMachine.getElasticServiceManager() != null) {
            return "ESM ";
        } else if (virtualMachine.getGridServiceManager() != null) {
            return "GSM ";
        } else if (virtualMachine.getGridServiceContainer() != null) {
            return "GSC ";
        } else if (virtualMachine.getGridServiceAgent() != null) {
            return "GSA ";
        } else if (virtualMachine.getLookupService() != null) {
            return "LUS ";
        } else return "";
    }

    /**
     * Returns the full name of a grid component running inside a JVM.
     * Inspects the JVM in the following order - GSM, GSC, GSA, LUS.
     * 
     * @param virtualMachine
     * @return full name of a grid component. "n/a" if no component found.
     */
    public static String getGridComponentFullName(VirtualMachine virtualMachine) {
        if (virtualMachine.getElasticServiceManager() != null) {
            return "Elastic Service Manager ";
        } else if (virtualMachine.getGridServiceManager() != null) {
            return "Grid Service Manager";
        } else if (virtualMachine.getGridServiceContainer() != null) {
            return "Grid Service Container";
        } else if (virtualMachine.getGridServiceAgent() != null) {
            return "Grid Service Agent";
        } else if (virtualMachine.getLookupService() != null) {
            return "Lookup Service";
        } else return "n/a";
    }
}
