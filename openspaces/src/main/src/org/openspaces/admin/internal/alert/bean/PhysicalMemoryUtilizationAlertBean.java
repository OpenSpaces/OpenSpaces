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
import org.openspaces.admin.alert.alerts.PhysicalMemoryUtilizationAlert;
import org.openspaces.admin.alert.config.PhysicalMemoryUtilizationAlertConfiguration;
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

public class PhysicalMemoryUtilizationAlertBean implements AlertBean,
        OperatingSystemStatisticsChangedEventListener, MachineRemovedEventListener {

    public static final String beanUID = "726a2752-4cae5258-f281-49d3-96b6-1e68e42bbd2c";
    public static final String ALERT_NAME = "Physical Memory Utilization";
    
    private final PhysicalMemoryUtilizationAlertConfiguration config = new PhysicalMemoryUtilizationAlertConfiguration();

    private Admin admin;
    private final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public PhysicalMemoryUtilizationAlertBean() {
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
        
        if (config.getHighThresholdPerc() == null) {
            throw new BeanConfigurationException("High threshold property is null");
        }
        
        if (config.getLowThresholdPerc() == null) {
            throw new BeanConfigurationException("Low threshold property is null");
        }
        
        if (config.getMeasurementPeriod() == null) {
            throw new BeanConfigurationException("Measurement period property is null");
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
        factory.description("Memory measurment is unavailable; machine has been removed");
        factory.severity(AlertSeverity.WARNING);
        factory.status(AlertStatus.NA);
        factory.componentUid(machine.getOperatingSystem().getUid());
        factory.componentDescription(AlertBeanUtils.getMachineDescription(machine));
        factory.config(config.getProperties());

        Alert alert = factory.toAlert();
        admin.getAlertManager().triggerAlert( new PhysicalMemoryUtilizationAlert(alert));
    }

    public void operatingSystemStatisticsChanged(OperatingSystemStatisticsChangedEvent event) {

        int highThreshold = config.getHighThresholdPerc();
        int lowThreshold = config.getLowThresholdPerc();
        
        double memoryAvg = calcAverageWithinPeriod(event);
        if (memoryAvg < 0) return; //period hasn't passed

        if (memoryAvg > highThreshold) {
            final String groupUid = generateGroupUid(event.getOperatingSystem().getUid());
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description("Memory crossed above a " + highThreshold + "% threshold, for a period of "
                    + TimeUtil.format(config.getMeasurementPeriod()) + ", with an average memory of " + NUMBER_FORMAT.format(memoryAvg) + "%");
            factory.severity(AlertSeverity.WARNING);
            factory.status(AlertStatus.RAISED);
            factory.componentUid(event.getOperatingSystem().getUid());
            factory.componentDescription(AlertBeanUtils.getMachineDescription(event.getStatistics().getDetails()));
            factory.config(config.getProperties());
            
            factory.putProperty(PhysicalMemoryUtilizationAlert.HOST_ADDRESS, event.getStatistics().getDetails().getHostAddress());
            factory.putProperty(PhysicalMemoryUtilizationAlert.HOST_NAME, event.getStatistics().getDetails().getHostName());
            factory.putProperty(PhysicalMemoryUtilizationAlert.CPU_UTILIZATION, String.valueOf(event.getStatistics().getCpuPerc()*100.0));
            factory.putProperty(PhysicalMemoryUtilizationAlert.MEMORY_UTILIZATION, String.valueOf(event.getStatistics().getPhysicalMemoryUsedPerc()));

            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new PhysicalMemoryUtilizationAlert(alert));
                
        } else if (memoryAvg < lowThreshold) {
            final String groupUid = generateGroupUid(event.getOperatingSystem().getUid());
            AlertHistory alertHistory = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertHistoryByGroupUid(groupUid);
            AlertHistoryDetails alertHistoryDetails = alertHistory.getDetails();
            if (alertHistoryDetails != null && !alertHistoryDetails.getLastAlertStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description("Memory crossed below a " + lowThreshold + "% threshold, for a period of "
                        + getPeriodOfTime(event) + ", with an average memory of " + NUMBER_FORMAT.format(memoryAvg) + "%");
                factory.severity(AlertSeverity.WARNING);
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(event.getOperatingSystem().getUid());
                factory.componentDescription(AlertBeanUtils.getMachineDescription(event.getStatistics().getDetails()));
                factory.config(config.getProperties());
                
                factory.putProperty(PhysicalMemoryUtilizationAlert.HOST_ADDRESS, event.getStatistics().getDetails().getHostAddress());
                factory.putProperty(PhysicalMemoryUtilizationAlert.HOST_NAME, event.getStatistics().getDetails().getHostName());
                factory.putProperty(PhysicalMemoryUtilizationAlert.CPU_UTILIZATION, String.valueOf(event.getStatistics().getCpuPerc()*100.0));
                factory.putProperty(PhysicalMemoryUtilizationAlert.MEMORY_UTILIZATION, String.valueOf(event.getStatistics().getPhysicalMemoryUsedPerc()));

                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert( new PhysicalMemoryUtilizationAlert(alert));
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
            if (!stats.isNA()) {
                timeline.add(stats.getPhysicalMemoryUsedPerc());
            }
        }
        
        return AlertBeanUtils.getAverage(period, timeline);
    }

    private String getPeriodOfTime(OperatingSystemStatisticsChangedEvent event) {
        return TimeUtil.format(config.getMeasurementPeriod());
    }
}
