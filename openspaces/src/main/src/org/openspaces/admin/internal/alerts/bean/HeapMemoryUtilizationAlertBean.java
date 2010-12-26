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
import org.openspaces.admin.alerts.config.PhysicalMemoryUtilizationAlertBeanConfig;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.alerts.AlertHistory;
import org.openspaces.admin.internal.alerts.AlertHistoryDetails;
import org.openspaces.admin.internal.alerts.InternalAlertManager;
import org.openspaces.admin.internal.alerts.bean.util.AlertBeanUtils;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachineStatistics;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventListener;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEvent;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventListener;

public class HeapMemoryUtilizationAlertBean implements AlertBean, VirtualMachineStatisticsChangedEventListener,
        VirtualMachineRemovedEventListener {

    public static final String beanUID = "694248f7-8a41119b-ddf9-4998-b3a0-885021e366af";
    public static final String ALERT_NAME = "Heap Memory Utilization";
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String PROCESS_ID = "process-id";
    public static final String COMPONENT_NAME = "component-name";
    public static final String MEMORY_UTILIZATION = "memory-utilization";
    
    private final PhysicalMemoryUtilizationAlertBeanConfig config = new PhysicalMemoryUtilizationAlertBeanConfig();

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
    

    public void virtualMachineRemoved(VirtualMachine virtualMachine) {

        final String groupUid = generateGroupUid(virtualMachine.getUid());
        AlertFactory factory = new AlertFactory();
        factory.name(ALERT_NAME);
        factory.beanConfigClass(config.getClass());
        factory.groupUid(groupUid);
        factory.description(getGridComponentShortName(virtualMachine)
                + "Heap memory is unavailable; has been removed");
        factory.severity(AlertSeverity.NA);
        factory.componentUid(virtualMachine.getUid());
        factory.properties(config.getProperties());
        factory.putProperty(MEMORY_UTILIZATION, "n/a");
        factory.putProperty(HOST_NAME, virtualMachine.getMachine().getHostName());
        factory.putProperty(HOST_ADDRESS, virtualMachine.getMachine().getHostAddress());
        factory.putProperty(PROCESS_ID, String.valueOf(virtualMachine.getDetails().getPid()));
        factory.putProperty(COMPONENT_NAME, getGridComponentFullName(virtualMachine));

        Alert alert = factory.toAlert();
        admin.getAlertManager().fireAlert(alert);
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
            factory.beanConfigClass(config.getClass());
            factory.groupUid(groupUid);
            factory.description(getGridComponentShortName(event.getVirtualMachine())
                    + "Heap memory crossed above a " + highThreshold + "% threshold, for a period of "
                    + getPeriodOfTime(event) + ", with an average memory of " + NUMBER_FORMAT.format(memoryAvg) + "%");
            factory.severity(AlertSeverity.WARNING);
            factory.componentUid(event.getVirtualMachine().getUid());
            factory.properties(config.getProperties());
            factory.putProperty(MEMORY_UTILIZATION, String.valueOf(memoryAvg));
            factory.putProperty(HOST_NAME, event.getVirtualMachine().getMachine().getHostName());
            factory.putProperty(HOST_ADDRESS, event.getVirtualMachine().getMachine().getHostAddress());
            factory.putProperty(PROCESS_ID, String.valueOf(event.getVirtualMachine().getDetails().getPid()));
            factory.putProperty(COMPONENT_NAME, getGridComponentFullName(event.getVirtualMachine()));

            Alert alert = factory.toAlert();
            admin.getAlertManager().fireAlert(alert);
                
        } else if (memoryAvg < lowThreshold) {
            final String groupUid = generateGroupUid(event.getVirtualMachine().getUid());
            AlertHistory alertHistory = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertHistoryByGroupUid(groupUid);
            AlertHistoryDetails alertHistoryDetails = alertHistory.getDetails();
            if (alertHistoryDetails != null && !alertHistoryDetails.isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.beanConfigClass(config.getClass());
                factory.groupUid(groupUid);
                factory.description(getGridComponentShortName(event.getVirtualMachine())
                        + "Heap memory crossed below a " + highThreshold + "% threshold, for a period of "
                        + getPeriodOfTime(event) + ", with an average memory of " + NUMBER_FORMAT.format(memoryAvg)
                        + "%");
                factory.severity(AlertSeverity.OK);
                factory.componentUid(event.getVirtualMachine().getUid());
                factory.properties(config.getProperties());
                factory.putProperty(MEMORY_UTILIZATION, String.valueOf(memoryAvg));
                factory.putProperty(HOST_NAME, event.getVirtualMachine().getMachine().getHostName());
                factory.putProperty(HOST_ADDRESS, event.getVirtualMachine().getMachine().getHostAddress());
                factory.putProperty(PROCESS_ID, String.valueOf(event.getVirtualMachine().getDetails().getPid()));
                factory.putProperty(COMPONENT_NAME, getGridComponentFullName(event.getVirtualMachine()));

                Alert alert = factory.toAlert();
                admin.getAlertManager().fireAlert(alert);
            }
        }
    }
    
    private String getGridComponentShortName(VirtualMachine virtualMachine) {
        if (virtualMachine.getGridServiceManager() != null) {
            return "GSM ";
        } else if (virtualMachine.getGridServiceContainer() != null) {
            return "GSC ";
        } else if (virtualMachine.getGridServiceAgent() != null) {
            return "GSA ";
        } else if (virtualMachine.getLookupService() != null) {
            return "LUS ";
        } else return "";
    }

    private String getGridComponentFullName(VirtualMachine virtualMachine) {
        if (virtualMachine.getGridServiceManager() != null) {
            return "Grid Service Manager";
        } else if (virtualMachine.getGridServiceContainer() != null) {
            return "Grid Service Container";
        } else if (virtualMachine.getGridServiceAgent() != null) {
            return "Grid Service Agent";
        } else if (virtualMachine.getLookupService() != null) {
            return "Lookup Service";
        } else return "n/a";
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
