package org.openspaces.admin.internal.alerts.bean;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import org.jini.rio.resources.util.TimeUtil;
import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.alerts.Alert;
import org.openspaces.admin.alerts.config.MachineCpuUtilizationAlertBeanConfig;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEvent;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventListener;

public class MachineCpuUtilizationAlertBean implements AlertBean,
        OperatingSystemStatisticsChangedEventListener {

    private final MachineCpuUtilizationAlertBeanConfig config = new MachineCpuUtilizationAlertBeanConfig();

    private final long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private Admin admin;
    private boolean inBetweenThresholdState = false;
    private final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public MachineCpuUtilizationAlertBean() {
        NUMBER_FORMAT.setMinimumFractionDigits(1);
        NUMBER_FORMAT.setMaximumFractionDigits(2);
    }

    public void afterPropertiesSet() throws Exception {
        validateProperties();
        
        admin.getOperatingSystems().getOperatingSystemStatisticsChanged().add(this);
        admin.getOperatingSystems().startStatisticsMonitor();
    }

    public void destroy() throws Exception {
        admin.getOperatingSystems().getOperatingSystemStatisticsChanged().remove(this);
        admin.getOperatingSystems().stopStatisticsMonitor();
    }

    public Map<String, String> getProperties() {
        return config.getProperties();
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public void setProperties(Map<String, String> properties) {
        config.setProperties(properties);
    }

    private void validateProperties() {
        
        try {
            config.getHighThresholdPerc();
            config.getLowThresholdPerc();
            config.getMeasurementPeriod();
        } catch (IllegalArgumentException e) {
            throw new BeanConfigurationException(e.getMessage());
        }

        if (config.getHighThresholdPerc() < config.getLowThresholdPerc()) {
            throw new BeanConfigurationException("Low threshold [" + config.getLowThresholdPerc()
                    + "%] must be less than high threshold value [" + config.getHighThresholdPerc() + "%]");
        }

        if (config.getHighThresholdPerc() < 0) {
            throw new BeanConfigurationException("High threshold [" + config.getHighThresholdPerc()
                    + "%] must greater than zero");
        }

        if (config.getLowThresholdPerc() < 0) {
            throw new BeanConfigurationException("Low threshold [" + config.getLowThresholdPerc()
                    + "%] must greater or equal to zero");
        }

        //TODO what if less than statistics interval?
        if (config.getMeasurementPeriod() < statisticsInterval) {
            throw new BeanConfigurationException("Measurment period [" + config.getMeasurementPeriod()
                    + " ms] must be greater than ["+statisticsInterval+" ms]");
        }
    }

    public void operatingSystemStatisticsChanged(OperatingSystemStatisticsChangedEvent event) {

        int highThreshold = config.getHighThresholdPerc();
        int lowThreshold = config.getLowThresholdPerc();
        
        double cpuAvg = calcAverageWithinPeriod(event);
        if (cpuAvg == -1) return; //period hasn't passed
        
        if (cpuAvg > highThreshold) {
                inBetweenThresholdState = true;
                Alert alert = new Alert();
                alert.setAlertDescription("CPU crossed above a " + highThreshold + "% threshold, for a period of "
                        + getPeriodOfTime(event) + ", with an average CPU of " + NUMBER_FORMAT.format(cpuAvg)
                        + "%");
                alert.setAlertType(this.getClass().getName());
                alert.setPositive(false);
                alert.setSourceComponentUid(event.getOperatingSystem().getUid());
                alert.setProperties(config.getProperties());
                alert.putProperty("utilization", String.valueOf(cpuAvg));
                alert.putProperty("hostname", event.getStatistics().getDetails().getHostName());
                alert.putProperty("hostAddress", event.getStatistics().getDetails().getHostAddress());

                admin.getAlertManager().fireAlert(alert);
        } else if (cpuAvg < lowThreshold) {
            if (inBetweenThresholdState) {
                inBetweenThresholdState = false;
                Alert alert = new Alert();
                alert.setAlertDescription("CPU crossed below a " + highThreshold + "% threshold, for a period of "
                        + getPeriodOfTime(event) + ", with an average CPU of " + NUMBER_FORMAT.format(cpuAvg)
                        + "%");
                alert.setAlertType(this.getClass().getName());
                alert.setPositive(true);
                alert.setSourceComponentUid(event.getOperatingSystem().getUid());
                alert.setProperties(config.getProperties());
                alert.putProperty("utilization", String.valueOf(cpuAvg));
                alert.putProperty("hostname", event.getStatistics().getDetails().getHostName());
                alert.putProperty("hostAddress", event.getStatistics().getDetails().getHostAddress());

                admin.getAlertManager().fireAlert(alert);
            }
        }
    }

    private double calcAverageWithinPeriod(OperatingSystemStatisticsChangedEvent event) {
        List<OperatingSystemStatistics> timeline = event.getStatistics().getTimeline();

        long measurementPeriod = config.getMeasurementPeriod();
        //TODO get the statistics interval from admin object
        long period = measurementPeriod / statisticsInterval;
        
        //not enough samples within requested period
        if (period > timeline.size()) return -1;
        
        double average = 0.0;
        for (int i = 0; i < period && i < timeline.size(); i++) {
            double cpuPerc = timeline.get(i).getCpuPerc();
            average += cpuPerc;
        }
        average /= period;
        
        return average;
    }

    private String getPeriodOfTime(OperatingSystemStatisticsChangedEvent event) {
        return TimeUtil.format(config.getMeasurementPeriod());
    }
}
