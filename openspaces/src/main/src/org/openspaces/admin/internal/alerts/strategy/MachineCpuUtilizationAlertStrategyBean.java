package org.openspaces.admin.internal.alerts.strategy;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import org.jini.rio.resources.util.TimeUtil;
import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.alerts.Alert;
import org.openspaces.admin.alerts.strategy.MachineCpuUtilizationAlertStrategyConfig;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEvent;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventListener;

public class MachineCpuUtilizationAlertStrategyBean implements AlertStrategyBean,
        OperatingSystemStatisticsChangedEventListener {

    private final MachineCpuUtilizationAlertStrategyConfig strategyConfig = new MachineCpuUtilizationAlertStrategyConfig();

    private final long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private Admin admin;
    private boolean inBetweenThresholdState = false;
    private final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public MachineCpuUtilizationAlertStrategyBean() {
        NUMBER_FORMAT.setMinimumFractionDigits(1);
        NUMBER_FORMAT.setMaximumFractionDigits(2);
    }

    public void afterPropertiesSet() {
        admin.getOperatingSystems().getOperatingSystemStatisticsChanged().add(this);
        admin.getOperatingSystems().startStatisticsMonitor();
    }

    public void destroy() {
        admin.getOperatingSystems().getOperatingSystemStatisticsChanged().remove(this);
        admin.getOperatingSystems().stopStatisticsMonitor();
    }

    public Map<String, String> getProperties() {
        return strategyConfig.getProperties();
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public void setProperties(Map<String, String> properties) {
        strategyConfig.setProperties(properties);
        validateProperties();
    }

    private void validateProperties() {
        if (strategyConfig.getHighThreshold() < strategyConfig.getLowThreshold()) {
            throw new IllegalArgumentException("Low threshold [" + strategyConfig.getLowThreshold()
                    + "] must be less than high threshold value [" + strategyConfig.getHighThreshold() + "]");
        }
        
        if (strategyConfig.getHighThreshold() < 0) {
            throw new IllegalArgumentException("High threshold [" + strategyConfig.getHighThreshold()
                    + "] must greater than zero");
        }
        
        if (strategyConfig.getLowThreshold() < 0) {
            throw new IllegalArgumentException("Low threshold [" + strategyConfig.getLowThreshold()
                    + "] must greater or equal to zero");
        }
        
        if (strategyConfig.getMovingAveragePeriod() < 1) {
            throw new IllegalArgumentException("Moving average period [" + strategyConfig.getMovingAveragePeriod()
                    + "] must greater than zero");
        }
    }

    public void operatingSystemStatisticsChanged(OperatingSystemStatisticsChangedEvent event) {

        int highThreshold = strategyConfig.getHighThreshold();
        int lowThreshold = strategyConfig.getLowThreshold();
        int movingAveragePeriod = strategyConfig.getMovingAveragePeriod();
        
        double cpuPerc = event.getStatistics().getCpuPerc() * 100;
        if (cpuPerc > highThreshold) {
            double cpuMovingAvg = calcSimpleMovingAverage(event);
            // System.out.println(cpuMovingAvg);
            if (cpuMovingAvg > highThreshold) {
                inBetweenThresholdState = true;
                Alert alert = new Alert();
                alert.setAlertDescription("CPU crossed above a " + highThreshold + "% threshold, for a period of "
                        + getPeriodOfTime(event) + ", with an average CPU of " + NUMBER_FORMAT.format(cpuMovingAvg)
                        + "%");
                alert.setAlertType(this.getClass().getName());
                alert.setPositive(false);
                alert.setSourceComponentUid(event.getOperatingSystem().getUid());
                alert.setProperty("highThreshold", highThreshold);
                alert.setProperty("lowThreshold", lowThreshold);
                alert.setProperty("movingAveragePeriod", movingAveragePeriod);
//                alert.setProperty("statisticsInterval", statisticsInterval);
                alert.setProperty("utilization", cpuMovingAvg);
                alert.setProperty("hostname", event.getStatistics().getDetails().getHostName());
                alert.setProperty("hostAddress", event.getStatistics().getDetails().getHostAddress());

                admin.getAlertManager().fireAlert(alert);
            }
        } else if (cpuPerc < lowThreshold) {
            double cpuMovingAvg = calcSimpleMovingAverage(event);
            // System.out.println(cpuMovingAvg);
            if (inBetweenThresholdState && cpuMovingAvg < lowThreshold) {
                inBetweenThresholdState = false;
                Alert alert = new Alert();
                alert.setAlertDescription("CPU crossed below a " + highThreshold + "% threshold, for a period of "
                        + getPeriodOfTime(event) + ", with an average CPU of " + NUMBER_FORMAT.format(cpuMovingAvg)
                        + "%");
                alert.setAlertType(this.getClass().getName());
                alert.setPositive(true);
                alert.setSourceComponentUid(event.getOperatingSystem().getUid());
                alert.setProperty("highThreshold", highThreshold);
                alert.setProperty("lowThreshold", lowThreshold);
                alert.setProperty("movingAveragePeriod", movingAveragePeriod);
//                alert.setProperty("statisticsInterval", statisticsInterval);
                alert.setProperty("utilization", cpuMovingAvg);
                alert.setProperty("hostname", event.getStatistics().getDetails().getHostName());
                alert.setProperty("hostAddress", event.getStatistics().getDetails().getHostAddress());

                admin.getAlertManager().fireAlert(alert);
            }
        }
    }

    private String getPeriodOfTime(OperatingSystemStatisticsChangedEvent event) {
        return TimeUtil.format(strategyConfig.getMovingAveragePeriod() * statisticsInterval);
    }

    private double calcSimpleMovingAverage(OperatingSystemStatisticsChangedEvent event) {
        List<OperatingSystemStatistics> timeline = event.getStatistics().getTimeline();
        double simpleMovingAverage = 100.0 * SMA(timeline);
        return simpleMovingAverage;
    }

    private double SMA(List<OperatingSystemStatistics> timeline) {
        int movingAveragePeriod = strategyConfig.getMovingAveragePeriod();
        double SMA = 0.0;
        for (int i = 0; i < movingAveragePeriod && i < timeline.size(); i++) {
            double cpuPerc = timeline.get(i).getCpuPerc();
            SMA += cpuPerc;
        }
        SMA /= movingAveragePeriod;
        return SMA;
    }

    // result=(Today’s Last – Yesterday’s EMA) x (Smoothing constant) + Yesterday’s EMA
    // where Smoothing constant=2 / (1 + N)
    // N is equal to the number of time periods for the EMA
    private double calcExponentialMovingAverage(OperatingSystemStatisticsChangedEvent event) {
        List<OperatingSystemStatistics> timeline = event.getStatistics().getTimeline();
        double exponentialMovingAverage = 100.0 * EMA(timeline, 0);
        return exponentialMovingAverage;
    }

    private double EMA(List<OperatingSystemStatistics> timeline, int index) {
        int movingAveragePeriod = strategyConfig.getMovingAveragePeriod();
        double EMA = 0.0;
        if (index < movingAveragePeriod && index < timeline.size()) {
            double todaysLast = timeline.get(index).getCpuPerc();
            double yesterdaysEMA = EMA(timeline, index + 1);
            EMA = (todaysLast - yesterdaysEMA) * (smoothingConstant()) + yesterdaysEMA;
        }
        return EMA;
    }

    private double smoothingConstant() {
        int movingAveragePeriod = strategyConfig.getMovingAveragePeriod();
        return 2.0 / (movingAveragePeriod + 1);
    }
}
