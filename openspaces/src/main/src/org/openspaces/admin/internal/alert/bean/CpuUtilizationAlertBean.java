package org.openspaces.admin.internal.alert.bean;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jini.rio.resources.util.TimeUtil;
import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.CpuUtilizationAlert;
import org.openspaces.admin.alert.config.CpuUtilizationAlertBeanConfig;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.alert.AlertHistory;
import org.openspaces.admin.internal.alert.AlertHistoryDetails;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.internal.alert.bean.util.AlertBeanUtils;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineRemovedEventListener;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEvent;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventListener;

public class CpuUtilizationAlertBean implements AlertBean,
        OperatingSystemStatisticsChangedEventListener, MachineRemovedEventListener {

    public static final String beanUID = "d7f14ccb-774a468d-29dd-4c23-b7de-d0ae9aaec204";
    public static final String ALERT_NAME = "CPU Utilization";
    
    private final CpuUtilizationAlertBeanConfig config = new CpuUtilizationAlertBeanConfig();

    private Admin admin;
    private final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public CpuUtilizationAlertBean() {
        NUMBER_FORMAT.setMinimumFractionDigits(1);
        NUMBER_FORMAT.setMaximumFractionDigits(2);
    }

    public void afterPropertiesSet() throws Exception {
        validateProperties();
        
        admin.getMachines().getMachineRemoved().add(this);
        admin.getOperatingSystems().getOperatingSystemStatisticsChanged().add(this);
        admin.getOperatingSystems().startStatisticsMonitor();
    }

    public void destroy() throws Exception {
        admin.getMachines().getMachineRemoved().remove(this);
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

        if (config.getMeasurementPeriod() < StatisticsMonitor.DEFAULT_MONITOR_INTERVAL) {
            throw new BeanConfigurationException("Measurment period [" + config.getMeasurementPeriod()
                    + " ms] must be greater than ["+StatisticsMonitor.DEFAULT_MONITOR_INTERVAL+" ms]");
        }
    }
    
    //unreachable machine
    public void machineRemoved(final Machine machine) {
        final String groupUid = generateGroupUid(machine.getOperatingSystem().getUid());
        AlertFactory factory = new AlertFactory();
        factory.name(ALERT_NAME);
        factory.groupUid(groupUid);
        factory.description("CPU measurment is unavailable; machine has been removed");
        factory.severity(AlertSeverity.WARNING);
        factory.status(AlertStatus.NA);
        factory.componentUid(machine.getOperatingSystem().getUid());
        factory.properties(config.getProperties());
        factory.putProperty(CpuUtilizationAlert.CPU_UTILIZATION, "n/a");
        factory.putProperty(CpuUtilizationAlert.HOST_NAME, machine.getHostName());
        factory.putProperty(CpuUtilizationAlert.HOST_ADDRESS, machine.getHostAddress());

        Alert alert = factory.toAlert();
        admin.getAlertManager().fireAlert( new CpuUtilizationAlert(alert));
    }

    public void operatingSystemStatisticsChanged(OperatingSystemStatisticsChangedEvent event) {

        int highThreshold = config.getHighThresholdPerc();
        int lowThreshold = config.getLowThresholdPerc();
        
        double cpuAvg = calcAverageWithinPeriod(event);
        if (cpuAvg < 0) return; //period hasn't passed

        if (cpuAvg > highThreshold) {
            final String groupUid = generateGroupUid(event.getOperatingSystem().getUid());
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description("CPU crossed above a " + highThreshold + "% threshold, for a period of "
                    + TimeUtil.format(config.getMeasurementPeriod()) + ", with an average CPU of " + NUMBER_FORMAT.format(cpuAvg) + "%");
            factory.severity(AlertSeverity.WARNING);
            factory.status(AlertStatus.RAISED);
            factory.componentUid(event.getOperatingSystem().getUid());
            factory.properties(config.getProperties());
            factory.putProperty(CpuUtilizationAlert.CPU_UTILIZATION, String.valueOf(cpuAvg));
            factory.putProperty(CpuUtilizationAlert.HOST_NAME, event.getStatistics().getDetails().getHostName());
            factory.putProperty(CpuUtilizationAlert.HOST_ADDRESS, event.getStatistics().getDetails().getHostAddress());

            Alert alert = factory.toAlert();
            admin.getAlertManager().fireAlert( new CpuUtilizationAlert(alert));
                
        } else if (cpuAvg < lowThreshold) {
            final String groupUid = generateGroupUid(event.getOperatingSystem().getUid());
            AlertHistory alertHistory = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertHistoryByGroupUid(groupUid);
            AlertHistoryDetails alertHistoryDetails = alertHistory.getDetails();
            if (alertHistoryDetails != null && !alertHistoryDetails.getLastAlertStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description("CPU crossed below a " + highThreshold + "% threshold, for a period of "
                        + TimeUtil.format(config.getMeasurementPeriod()) + ", with an average CPU of " + NUMBER_FORMAT.format(cpuAvg) + "%");
                factory.severity(AlertSeverity.WARNING);
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(event.getOperatingSystem().getUid());
                factory.properties(config.getProperties());
                factory.putProperty(CpuUtilizationAlert.CPU_UTILIZATION, String.valueOf(cpuAvg));
                factory.putProperty(CpuUtilizationAlert.HOST_NAME, event.getStatistics().getDetails().getHostName());
                factory.putProperty(CpuUtilizationAlert.HOST_ADDRESS, event.getStatistics().getDetails().getHostAddress());

                Alert alert = factory.toAlert();
                admin.getAlertManager().fireAlert( new CpuUtilizationAlert(alert));
            }
        }
    }

    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }

    private double calcAverageWithinPeriod(OperatingSystemStatisticsChangedEvent event) {
        long measurementPeriod = config.getMeasurementPeriod();
        int period = (int) (measurementPeriod / StatisticsMonitor.DEFAULT_MONITOR_INTERVAL);
        
        List<Double> timeline = new ArrayList<Double>(event.getStatistics().getTimeline().size());
        for (OperatingSystemStatistics stats : event.getStatistics().getTimeline()) {
            timeline.add(stats.getCpuPerc()*100.0);
        }
        
        return AlertBeanUtils.getAverage(period, timeline);
    }
}
