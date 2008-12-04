package org.openspaces.admin;

import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface StatisticsMonitor {

    static final long DEFAULT_MONITOR_INTERVAL = 5000;

    void setStatisticsInterval(long interval, TimeUnit timeUnit);

    void startStatisticsMonitor();

    void stopStatisticsMontior();

    boolean isMonitoring();
}
