package org.openspaces.admin.internal.alert.bean;

import java.text.NumberFormat;
import java.util.Map;

import org.jini.rio.resources.util.TimeUtil;
import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.config.GarbageCollectionPauseAlertBeanConfig;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.alert.AlertHistory;
import org.openspaces.admin.internal.alert.AlertHistoryDetails;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.internal.alert.bean.util.AlertBeanUtils;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventListener;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEvent;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventListener;

public class GarbageCollectionPauseAlertBean implements AlertBean, VirtualMachineStatisticsChangedEventListener,
        VirtualMachineRemovedEventListener {

    public static final String beanUID = "cccc5549-4c0f6dce-8c36-4984-bd6e-9caee8fbf843";
    public static final String ALERT_NAME = "GC Pause";
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String PROCESS_ID = "process-id";
    public static final String COMPONENT_NAME = "component-name";
    public static final String GC_PAUSE_TIME_MILLISECONDS = "gc-pause-time-milliseconds";
    public static final String CPU_UTILIZATION = "cpu-utilization";
    public static final String HEAP_UTILIZATION = "heap-utilization";
    public static final String NON_HEAP_UTILIZATION = "nonheap-utilization";
    
    
    private final GarbageCollectionPauseAlertBeanConfig config = new GarbageCollectionPauseAlertBeanConfig();

    private Admin admin;
    private final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public GarbageCollectionPauseAlertBean() {
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
        
        if (config.getLongGcPausePeriod() < config.getShortGcPausePeriod()) {
            throw new BeanConfigurationException("Long GC period [" + config.getLongGcPausePeriod()
                    + " ms] must be greater than the short GC period [" + config.getShortGcPausePeriod() + " ms]");
        }
        
        if (config.getLongGcPausePeriod() < StatisticsMonitor.DEFAULT_MONITOR_INTERVAL) {
            throw new BeanConfigurationException("Measurment period [" + config.getLongGcPausePeriod()
                    + " ms] must be greater than ["+StatisticsMonitor.DEFAULT_MONITOR_INTERVAL+" ms]");
        }
        
        if (config.getShortGcPausePeriod() < 0) {
            throw new BeanConfigurationException("Measurment period [" + config.getShortGcPausePeriod()
                    + " ms] must be greater than zero");
        }
    }
    

    public void virtualMachineRemoved(VirtualMachine virtualMachine) {

        final String groupUid = generateGroupUid(virtualMachine.getUid());
        AlertFactory factory = new AlertFactory();
        factory.name(ALERT_NAME);
        factory.groupUid(groupUid);
        factory.description(AlertBeanUtils.getGridComponentShortName(virtualMachine)
                + "GC reading is unavailable; JVM has been removed");
        factory.severity(AlertSeverity.WARNING);
        factory.status(AlertStatus.NA);
        factory.componentUid(virtualMachine.getUid());
        factory.config(config.getProperties());
        factory.putProperty(GC_PAUSE_TIME_MILLISECONDS, "n/a");
        factory.putProperty(HOST_NAME, virtualMachine.getMachine().getHostName());
        factory.putProperty(HOST_ADDRESS, virtualMachine.getMachine().getHostAddress());
        factory.putProperty(PROCESS_ID, String.valueOf(virtualMachine.getDetails().getPid()));
        factory.putProperty(COMPONENT_NAME, AlertBeanUtils.getGridComponentFullName(virtualMachine));

        Alert alert = factory.toAlert();
        admin.getAlertManager().fireAlert(alert);
    }

    public void virtualMachineStatisticsChanged(VirtualMachineStatisticsChangedEvent event) {

        long longGcPausePeriod = config.getLongGcPausePeriod();
        long shortGcPausePeriod = config.getShortGcPausePeriod();
        long gcCollectionTime = event.getStatistics().getGcCollectionTime();
        if (event.getStatistics().getPrevious() == null)  
            return; //wait for more stats
        
        long prevGcCollectionTime = event.getStatistics().getPrevious().getGcCollectionTime();
        long diffGcCollectionTime = gcCollectionTime - prevGcCollectionTime;

        if (diffGcCollectionTime == 0) 
            return; //same value, no change
        
      if (AlertBeanUtils.getGridComponentShortName(event.getVirtualMachine()).equals("GSC "))
      System.out.println("gc collection time: " + diffGcCollectionTime);
        
        if (diffGcCollectionTime > longGcPausePeriod) {
            final String groupUid = generateGroupUid(event.getVirtualMachine().getUid());
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description(AlertBeanUtils.getGridComponentShortName(event.getVirtualMachine())
                    + "JVM garbage cycle collection time took " + TimeUtil.format(diffGcCollectionTime));
            factory.severity(AlertSeverity.WARNING);
            factory.status(AlertStatus.RAISED);
            factory.componentUid(event.getVirtualMachine().getUid());
            factory.config(config.getProperties());
            factory.putProperty(GC_PAUSE_TIME_MILLISECONDS, String.valueOf(diffGcCollectionTime));
            factory.putProperty(CPU_UTILIZATION, String.valueOf(event.getStatistics().getCpuPerc()*100.0));
            factory.putProperty(HEAP_UTILIZATION, String.valueOf(event.getStatistics().getMemoryHeapUsedPerc()));
            factory.putProperty(NON_HEAP_UTILIZATION, String.valueOf(event.getStatistics().getMemoryNonHeapUsedPerc()));
            factory.putProperty(HOST_NAME, event.getVirtualMachine().getMachine().getHostName());
            factory.putProperty(HOST_ADDRESS, event.getVirtualMachine().getMachine().getHostAddress());
            factory.putProperty(PROCESS_ID, String.valueOf(event.getVirtualMachine().getDetails().getPid()));
            factory.putProperty(COMPONENT_NAME, AlertBeanUtils.getGridComponentFullName(event.getVirtualMachine()));

            Alert alert = factory.toAlert();
            admin.getAlertManager().fireAlert(alert);
        } else if (diffGcCollectionTime < shortGcPausePeriod) {
            final String groupUid = generateGroupUid(event.getVirtualMachine().getUid());
            AlertHistory alertHistory = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertHistoryByGroupUid(groupUid);
            AlertHistoryDetails alertHistoryDetails = alertHistory.getDetails();
            if (alertHistoryDetails != null && !alertHistoryDetails.getLastAlertStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description(AlertBeanUtils.getGridComponentShortName(event.getVirtualMachine())
                        + "JVM garbage cycle collection time took " + TimeUtil.format(diffGcCollectionTime));
                factory.severity(AlertSeverity.WARNING);
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(event.getVirtualMachine().getUid());
                factory.config(config.getProperties());
                factory.putProperty(GC_PAUSE_TIME_MILLISECONDS, String.valueOf(diffGcCollectionTime));
                factory.putProperty(CPU_UTILIZATION, String.valueOf(event.getStatistics().getCpuPerc()*100.0));
                factory.putProperty(HEAP_UTILIZATION, String.valueOf(event.getStatistics().getMemoryHeapUsedPerc()));
                factory.putProperty(NON_HEAP_UTILIZATION, String.valueOf(event.getStatistics().getMemoryNonHeapUsedPerc()));
                factory.putProperty(HOST_NAME, event.getVirtualMachine().getMachine().getHostName());
                factory.putProperty(HOST_ADDRESS, event.getVirtualMachine().getMachine().getHostAddress());
                factory.putProperty(PROCESS_ID, String.valueOf(event.getVirtualMachine().getDetails().getPid()));
                factory.putProperty(COMPONENT_NAME, AlertBeanUtils.getGridComponentFullName(event.getVirtualMachine()));

                Alert alert = factory.toAlert();
                admin.getAlertManager().fireAlert(alert);
            }
        }
    }

    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }
}
