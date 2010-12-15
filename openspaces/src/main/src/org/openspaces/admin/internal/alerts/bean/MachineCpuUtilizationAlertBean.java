package org.openspaces.admin.internal.alerts.bean;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jini.rio.resources.util.TimeUtil;
import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.alerts.Alert;
import org.openspaces.admin.alerts.AlertFactory;
import org.openspaces.admin.alerts.AlertSeverity;
import org.openspaces.admin.alerts.config.MachineCpuUtilizationAlertBeanConfig;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.alerts.bean.util.AlertBeanUtils;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEvent;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventListener;

public class MachineCpuUtilizationAlertBean implements AlertBean,
        OperatingSystemStatisticsChangedEventListener {

    public final static String beanUID = "dc675afe-6a81-4400-957c-93392a70de4c";
    
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

                AlertFactory factory = new AlertFactory();
                factory.name("Machine CPU Utilization");
                factory.beanClassName(this.getClass().getName());
                factory.groupUid(beanUID.concat("-").concat(event.getOperatingSystem().getUid()));
                factory.description("CPU crossed above a " + highThreshold + "% threshold, for a period of "
                        + getPeriodOfTime(event) + ", with an average CPU of " + NUMBER_FORMAT.format(cpuAvg) + "%");
                factory.severity(AlertSeverity.CRITICAL);
                factory.componentUid(event.getOperatingSystem().getUid());
                factory.properties(config.getProperties());
                factory.putProperty("cpu-utilization", String.valueOf(cpuAvg));
                factory.putProperty("hostname", event.getStatistics().getDetails().getHostName());
                factory.putProperty("host-address", event.getStatistics().getDetails().getHostAddress());

                Alert alert = factory.toAlert();
                admin.getAlertManager().fireAlert(alert);
                
        } else if (cpuAvg < lowThreshold) {
            if (inBetweenThresholdState) {
                inBetweenThresholdState = false;
                
                AlertFactory factory = new AlertFactory();
                factory.name("Machine CPU Utilization");
                factory.beanClassName(this.getClass().getName());
                factory.groupUid(beanUID.concat("-").concat(event.getOperatingSystem().getUid()));
                factory.description("CPU crossed below a " + highThreshold + "% threshold, for a period of "
                        + getPeriodOfTime(event) + ", with an average CPU of " + NUMBER_FORMAT.format(cpuAvg) + "%");
                factory.severity(AlertSeverity.OK);
                factory.componentUid(event.getOperatingSystem().getUid());
                factory.properties(config.getProperties());
                factory.putProperty("cpu-utilization", String.valueOf(cpuAvg));
                factory.putProperty("hostname", event.getStatistics().getDetails().getHostName());
                factory.putProperty("host-address", event.getStatistics().getDetails().getHostAddress());

                Alert alert = factory.toAlert();
                admin.getAlertManager().fireAlert(alert);
            }
        }
    }

    private double calcAverageWithinPeriod(OperatingSystemStatisticsChangedEvent event) {

        //TODO get the statistics interval from admin object
        long measurementPeriod = config.getMeasurementPeriod();
        int period = (int) (measurementPeriod / statisticsInterval);
        
        List<Double> timeline = new ArrayList<Double>(event.getStatistics().getTimeline().size());
        for (OperatingSystemStatistics stats : event.getStatistics().getTimeline()) {
            timeline.add(stats.getCpuPerc()*100.0);
        }
        
        return AlertBeanUtils.getAverage(period, timeline);
    }

    private String getPeriodOfTime(OperatingSystemStatisticsChangedEvent event) {
        return TimeUtil.format(config.getMeasurementPeriod());
    }
}
