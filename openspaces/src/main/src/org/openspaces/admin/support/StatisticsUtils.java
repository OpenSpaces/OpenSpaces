package org.openspaces.admin.support;

/**
 * @author kimchy
 */
public class StatisticsUtils {

    public static double computePerc(int value, int max) {
        return ((double) value) / max * 100;
    }

    public static double computePerc(long value, long max) {
        return ((double) value) / max * 100;
    }

    public static double computePercByTime(long currentTime, long previousTime, long currentTimestamp, long previousTimestamp) {
        return ((double)(currentTime - previousTime)) / (currentTimestamp - previousTimestamp);
    }

    public static double computePerSecond(long currentCount, long previousCount, long currentTimestamp, long previousTimestamp) {
        return ((double) (currentCount - previousCount)) / (currentTimestamp - previousTimestamp) * 1000;
    }
}
