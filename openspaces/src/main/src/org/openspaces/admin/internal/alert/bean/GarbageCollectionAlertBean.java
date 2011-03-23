package org.openspaces.admin.internal.alert.bean;

import java.text.NumberFormat;
import java.util.Map;

import org.jini.rio.resources.util.TimeUtil;
import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.GarbageCollectionAlert;
import org.openspaces.admin.alert.config.GarbageCollectionAlertConfiguration;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.alert.AlertHistory;
import org.openspaces.admin.internal.alert.AlertHistoryDetails;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.internal.alert.bean.util.AlertBeanUtils;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventListener;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEvent;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventListener;

public class GarbageCollectionAlertBean implements AlertBean, VirtualMachineStatisticsChangedEventListener,
        VirtualMachineRemovedEventListener {

    public static final String beanUID = "94e663d9-0e2877c3-beb0-473f-a5bb-8fe965cd8751";
    public static final String ALERT_NAME = "Garbage Collection";
    
    private final GarbageCollectionAlertConfiguration config = new GarbageCollectionAlertConfiguration();

    private Admin admin;
    private final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public GarbageCollectionAlertBean() {
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
        
        if (config.getLongGcPausePeriod() == null) {
            throw new BeanConfigurationException("Long GC pause pertiod property is null");
        }
        
        if (config.getShortGcPausePeriod() == null) {
            throw new BeanConfigurationException("Short GC pause pertiod property is null");
        }
        
        if (config.getLongGcPausePeriod() < config.getShortGcPausePeriod()) {
            throw new BeanConfigurationException("Long GC period [" + config.getLongGcPausePeriod()
                    + " ms] must be greater than the short GC period [" + config.getShortGcPausePeriod() + " ms]");
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
                + " GC reading is unavailable; JVM has been removed");
        factory.severity(AlertSeverity.WARNING);
        factory.status(AlertStatus.NA);
        factory.componentUid(virtualMachine.getUid());
        factory.componentDescription(AlertBeanUtils.getGridComponentDescription(virtualMachine));
        factory.config(config.getProperties());

        Alert alert = factory.toAlert();
        admin.getAlertManager().triggerAlert( new GarbageCollectionAlert(alert));
    }

    public void virtualMachineStatisticsChanged(VirtualMachineStatisticsChangedEvent event) {

        long longGcPausePeriod = config.getLongGcPausePeriod();
        long shortGcPausePeriod = config.getShortGcPausePeriod();
        long gcCollectionTime = event.getStatistics().getGcCollectionTime();
        if (event.getStatistics().getPrevious() == null)  
            return; //wait for more stats
        
        long prevGcCollectionTime = event.getStatistics().getPrevious().getGcCollectionTime();
        long diffGcCollectionTime = gcCollectionTime - prevGcCollectionTime;
        long diffGcCollectionCount = event.getStatistics().getGcCollectionCount() - event.getStatistics().getPrevious().getGcCollectionCount();

        if (diffGcCollectionTime == 0 || diffGcCollectionCount == 0) 
            return; //same value, no change
              
        // divide total accumulated gc time by the number of accumulated collection count to get the
        // average gc reading for our measurement intervals
        long gcPauseTime = diffGcCollectionTime / diffGcCollectionCount;
        
        if (gcPauseTime > longGcPausePeriod) {
            final String groupUid = generateGroupUid(event.getVirtualMachine().getUid());
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description(AlertBeanUtils.getGridComponentShortName(event.getVirtualMachine())
                    + " JVM garbage cycle collection time took " + TimeUtil.format(gcPauseTime));
            factory.severity(AlertSeverity.WARNING);
            factory.status(AlertStatus.RAISED);
            factory.componentUid(event.getVirtualMachine().getUid());
            factory.componentDescription(AlertBeanUtils.getGridComponentDescription(event.getVirtualMachine()));
            factory.config(config.getProperties());
            
            factory.putProperty(GarbageCollectionAlert.HOST_ADDRESS, event.getVirtualMachine().getMachine().getHostAddress());
            factory.putProperty(GarbageCollectionAlert.HOST_NAME, event.getVirtualMachine().getMachine().getHostName());
            factory.putProperty(GarbageCollectionAlert.CPU_UTILIZATION, String.valueOf(event.getStatistics().getCpuPerc()*100.0));
            factory.putProperty(GarbageCollectionAlert.PROCESS_ID, String.valueOf(event.getVirtualMachine().getDetails().getPid()));
            factory.putProperty(GarbageCollectionAlert.COMPONENT_NAME, AlertBeanUtils.getGridComponentFullName(event.getVirtualMachine()));
            factory.putProperty(GarbageCollectionAlert.GC_DURATION_MILLISECONDS, String.valueOf(gcPauseTime));
            factory.putProperty(GarbageCollectionAlert.HEAP_UTILIZATION, String.valueOf(event.getStatistics().getMemoryHeapUsedPerc()));
            factory.putProperty(GarbageCollectionAlert.NON_HEAP_UTILIZATION, String.valueOf(event.getStatistics().getMemoryNonHeapUsedPerc()));


            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new GarbageCollectionAlert(alert));
            
        } else if (gcPauseTime < shortGcPausePeriod) {
            final String groupUid = generateGroupUid(event.getVirtualMachine().getUid());
            AlertHistory alertHistory = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertHistoryByGroupUid(groupUid);
            AlertHistoryDetails alertHistoryDetails = alertHistory.getDetails();
            if (alertHistoryDetails != null && !alertHistoryDetails.getLastAlertStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description(AlertBeanUtils.getGridComponentShortName(event.getVirtualMachine())
                        + " JVM garbage cycle collection time took " + TimeUtil.format(gcPauseTime));
                factory.severity(AlertSeverity.WARNING);
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(event.getVirtualMachine().getUid());
                factory.componentDescription(AlertBeanUtils.getGridComponentDescription(event.getVirtualMachine()));
                factory.config(config.getProperties());
                
                factory.putProperty(GarbageCollectionAlert.HOST_ADDRESS, event.getVirtualMachine().getMachine().getHostAddress());
                factory.putProperty(GarbageCollectionAlert.HOST_NAME, event.getVirtualMachine().getMachine().getHostName());
                factory.putProperty(GarbageCollectionAlert.CPU_UTILIZATION, String.valueOf(event.getStatistics().getCpuPerc()*100.0));
                factory.putProperty(GarbageCollectionAlert.PROCESS_ID, String.valueOf(event.getVirtualMachine().getDetails().getPid()));
                factory.putProperty(GarbageCollectionAlert.COMPONENT_NAME, AlertBeanUtils.getGridComponentFullName(event.getVirtualMachine()));
                factory.putProperty(GarbageCollectionAlert.GC_DURATION_MILLISECONDS, String.valueOf(gcPauseTime));
                factory.putProperty(GarbageCollectionAlert.HEAP_UTILIZATION, String.valueOf(event.getStatistics().getMemoryHeapUsedPerc()));
                factory.putProperty(GarbageCollectionAlert.NON_HEAP_UTILIZATION, String.valueOf(event.getStatistics().getMemoryNonHeapUsedPerc()));

                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert( new GarbageCollectionAlert(alert));
            }
        }
    }

    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }
}
