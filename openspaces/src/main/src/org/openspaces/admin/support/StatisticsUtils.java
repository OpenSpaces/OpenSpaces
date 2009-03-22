package org.openspaces.admin.support;

/**
 * @author kimchy
 */
public class StatisticsUtils {

    static double megabytesFactor = 9.53674316 * Math.pow(10, -7);

    static double gigabyesFactor = 9.53674316 * Math.pow(10, -10);

    public static double convertToKilobytes(long bytes) {
        return 0.0009765625 * bytes;
    }

    public static double convertToMB(long bytes) {
        return megabytesFactor * bytes;
    }

    public static double convertToGB(long bytes) {
        return gigabyesFactor * bytes;
    }

    public static double computePerc(int value, int max) {
        return ((double) value) / max * 100;
    }

    public static double computePerc(long value, long max) {
        return ((double) value) / max * 100;
    }

    public static double computePercByTime(long currentTime, long previousTime, long currentTimestamp, long previousTimestamp) {
        return ((double) (currentTime - previousTime)) / (currentTimestamp - previousTimestamp);
    }

    public static double computePerSecond(long currentCount, long previousCount, long currentTimestamp, long previousTimestamp) {
        return ((double) (currentCount - previousCount)) / (currentTimestamp - previousTimestamp) * 1000;
    }

    public static String formatPerc(double perc) {
        if (perc == -1) {
            return "NA";
        }
        String p = String.valueOf(perc * 100.0);
        int ix = p.indexOf(".") + 1;
        return p.substring(0, ix) + p.substring(ix, ix + 1) + '%';
    }
}
