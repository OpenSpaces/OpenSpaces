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
import org.openspaces.admin.alert.alerts.HeapMemoryUtilizationAlert;
import org.openspaces.admin.alert.config.PhysicalMemoryUtilizationAlertConfiguration;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.alert.AlertHistory;
import org.openspaces.admin.internal.alert.AlertHistoryDetails;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.internal.alert.bean.util.AlertBeanUtils;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachineStatistics;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventListener;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEvent;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventListener;

public class HeapMemoryUtilizationAlertBean implements AlertBean, VirtualMachineStatisticsChangedEventListener,
        VirtualMachineRemovedEventListener {

    public static final String beanUID = "694248f7-8a41119b-ddf9-4998-b3a0-885021e366af";
    public static final String ALERT_NAME = "Heap Memory Utilization";
    
    private final PhysicalMemoryUtilizationAlertConfiguration config = new PhysicalMemoryUtilizationAlertConfiguration();

    private Admin admin;
    private final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public HeapMemoryUtilizationAlertBean() {
        NUMBER_FORMAT.setMinimumFractionDigits(1);
        NUMBER_FORMAT.setMaximumFractionDigits(2);
    }

    public void afterPropertiesSet() throws Exception {
        validateProperties();
        
        admin.getVirtualMachines().getVirtualMachineRemoved().add(this);
        admin.getVirtualMachines().getVirtualMachineStatisticsChanged().add(this);
        admin.getVirtualMachines().startStatisticsMonitor();
    }

    public void destroy() throws Exception {
        admin.getVirtualMachines().getVirtualMachineRemoved().remove(this);
        admin.getVirtualMachines().getVirtualMachineStatisticsChanged().remove(this);
        admin.getVirtualMachines().stopStatisticsMonitor();
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
    

    public void virtualMachineRemoved(VirtualMachine virtualMachine) {

        final String groupUid = generateGroupUid(virtualMachine.getUid());
        AlertFactory factory = new AlertFactory();
        factory.name(ALERT_NAME);
        factory.groupUid(groupUid);
        factory.description(AlertBeanUtils.getGridComponentShortName(virtualMachine)
                + "Heap memory is unavailable; JVM has been removed");
        factory.severity(AlertSeverity.WARNING);
        factory.status(AlertStatus.NA);
        factory.componentUid(virtualMachine.getUid());
        factory.config(config.getProperties());

        Alert alert = factory.toAlert();
        admin.getAlertManager().fireAlert( new HeapMemoryUtilizationAlert(alert));
    }

    public void virtualMachineStatisticsChanged(VirtualMachineStatisticsChangedEvent event) {

        int highThreshold = config.getHighThresholdPerc();
        int lowThreshold = config.getLowThresholdPerc();
        
        double memoryAvg = calcAverageWithinPeriod(event);
        if (memoryAvg < 0) return; //period hasn't passed

        if (memoryAvg > highThreshold) {
            final String groupUid = generateGroupUid(event.getVirtualMachine().getUid());
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description(AlertBeanUtils.getGridComponentShortName(event.getVirtualMachine())
                    + "Heap memory crossed above a " + highThreshold + "% threshold, for a period of "
                    + TimeUtil.format(config.getMeasurementPeriod()) + ", with an average memory of " + NUMBER_FORMAT.format(memoryAvg) + "%");
            factory.severity(AlertSeverity.WARNING);
            factory.status(AlertStatus.RAISED);
            factory.componentUid(event.getVirtualMachine().getUid());
            factory.config(config.getProperties());
            
            factory.putProperty(HeapMemoryUtilizationAlert.HOST_ADDRESS, event.getVirtualMachine().getMachine().getHostAddress());
            factory.putProperty(HeapMemoryUtilizationAlert.HOST_NAME, event.getVirtualMachine().getMachine().getHostName());
            factory.putProperty(HeapMemoryUtilizationAlert.CPU_UTILIZATION, String.valueOf(event.getStatistics().getCpuPerc()*100.0));
            factory.putProperty(HeapMemoryUtilizationAlert.PROCESS_ID, String.valueOf(event.getVirtualMachine().getDetails().getPid()));
            factory.putProperty(HeapMemoryUtilizationAlert.COMPONENT_NAME, AlertBeanUtils.getGridComponentFullName(event.getVirtualMachine()));
            factory.putProperty(HeapMemoryUtilizationAlert.HEAP_UTILIZATION, String.valueOf(event.getStatistics().getMemoryHeapUsedPerc()));

            Alert alert = factory.toAlert();
            admin.getAlertManager().fireAlert( new HeapMemoryUtilizationAlert(alert));
                
        } else if (memoryAvg < lowThreshold) {
            final String groupUid = generateGroupUid(event.getVirtualMachine().getUid());
            AlertHistory alertHistory = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertHistoryByGroupUid(groupUid);
            AlertHistoryDetails alertHistoryDetails = alertHistory.getDetails();
            if (alertHistoryDetails != null && !alertHistoryDetails.getLastAlertStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description(AlertBeanUtils.getGridComponentShortName(event.getVirtualMachine())
                        + "Heap memory crossed below a " + lowThreshold + "% threshold, for a period of "
                        + getPeriodOfTime(event) + ", with an average memory of " + NUMBER_FORMAT.format(memoryAvg)
                        + "%");
                factory.severity(AlertSeverity.WARNING);
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(event.getVirtualMachine().getUid());
                factory.config(config.getProperties());
                
                factory.putProperty(HeapMemoryUtilizationAlert.HOST_ADDRESS, event.getVirtualMachine().getMachine().getHostAddress());
                factory.putProperty(HeapMemoryUtilizationAlert.HOST_NAME, event.getVirtualMachine().getMachine().getHostName());
                factory.putProperty(HeapMemoryUtilizationAlert.CPU_UTILIZATION, String.valueOf(event.getStatistics().getCpuPerc()*100.0));
                factory.putProperty(HeapMemoryUtilizationAlert.PROCESS_ID, String.valueOf(event.getVirtualMachine().getDetails().getPid()));
                factory.putProperty(HeapMemoryUtilizationAlert.COMPONENT_NAME, AlertBeanUtils.getGridComponentFullName(event.getVirtualMachine()));
                factory.putProperty(HeapMemoryUtilizationAlert.HEAP_UTILIZATION, String.valueOf(event.getStatistics().getMemoryHeapUsedPerc()));

                Alert alert = factory.toAlert();
                admin.getAlertManager().fireAlert( new HeapMemoryUtilizationAlert(alert));
            }
        }
    }

    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }

    private double calcAverageWithinPeriod(VirtualMachineStatisticsChangedEvent event) {
        long measurementPeriod = config.getMeasurementPeriod();
        int period = (int) (measurementPeriod / StatisticsMonitor.DEFAULT_MONITOR_INTERVAL);
        
        List<Double> timeline = new ArrayList<Double>(event.getStatistics().getTimeline().size());
        for (VirtualMachineStatistics stats : event.getStatistics().getTimeline()) {
            timeline.add(stats.getMemoryHeapUsedPerc());
        }
        
        return AlertBeanUtils.getAverage(period, timeline);
    }

    private String getPeriodOfTime(VirtualMachineStatisticsChangedEvent event) {
        return TimeUtil.format(config.getMeasurementPeriod());
    }
}
