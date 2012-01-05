package org.openspaces.admin.internal.alert.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.ProcessingUnitInstanceMemberAliveIndicatorAlert;
import org.openspaces.admin.alert.config.ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.MemberAliveIndicatorStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;

public class ProcessingUnitInstanceMemberAliveIndicatorAlertBean implements AlertBean,
        ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener, ProcessingUnitRemovedEventListener {

    public static final String beanUID = "c5a43e4-39eaf476-fd5d-4399-8b7b-a97c3ec4f49a";
    public static final String ALERT_NAME = "Member Alive Indicator";
    
    private final ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration config = new ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration();
    private final Map<String, List<ProcessingUnitInstance>> mapProcessingUnitNameToProcessingUnitInstances = new HashMap<String, List<ProcessingUnitInstance>>();

    private Admin admin;

    public ProcessingUnitInstanceMemberAliveIndicatorAlertBean() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        validateProperties();
        admin.getProcessingUnits().getProcessingUnitRemoved().add(this);
        admin.getProcessingUnits().getProcessingUnitInstanceMemberAliveIndicatorStatusChanged().add(this);
    }

    @Override
    public void destroy() throws Exception {
        admin.getProcessingUnits().getProcessingUnitRemoved().remove(this);
        admin.getProcessingUnits().getProcessingUnitInstanceMemberAliveIndicatorStatusChanged().remove(this);
    }

    @Override
    public Map<String, String> getProperties() {
        return config.getProperties();
    }

    @Override
    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        config.setProperties(properties);
    }

    private void validateProperties() {
    }
    
    @Override
    public void processingUnitRemoved(ProcessingUnit processingUnit) {
        List<ProcessingUnitInstance> listOfProcessingUnitInstances = mapProcessingUnitNameToProcessingUnitInstances.get(processingUnit.getName());
        if (listOfProcessingUnitInstances == null) {
            return;
        }
        for (ProcessingUnitInstance processingUnitInstance : listOfProcessingUnitInstances) {
            final String groupUid = generateGroupUid(processingUnitInstance.getUid());

            Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
            if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                
                if (processingUnit.getStatus().equals(DeploymentStatus.UNDEPLOYED)) {
                    factory.description("Processing Unit " + processingUnitInstance.getProcessingUnit().getName() + " has been undeployed");
                    factory.severity(AlertSeverity.INFO);
                } else {
                    factory.description("Processing Unit " + processingUnitInstance.getProcessingUnit().getName() + " has been removed");
                    factory.severity(AlertSeverity.SEVERE);
                }

                factory.status(AlertStatus.NA);
                factory.componentUid(processingUnitInstance.getUid());
                factory.componentDescription(processingUnitInstance.getProcessingUnitInstanceName());
                factory.config(config.getProperties());

                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert( new ProcessingUnitInstanceMemberAliveIndicatorAlert(alert));
            } 
        }

        mapProcessingUnitNameToProcessingUnitInstances.remove(processingUnit.getName());
        
    }
    
    @Override
    public void processingUnitInstanceMemberAliveIndicatorStatusChanged(
            ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent event) {

        final String groupUid = generateGroupUid(event.getProcessingUnitInstance().getUid());

        if (event.getNewStatus().equals(MemberAliveIndicatorStatus.ALIVE)) {
            /*
             * resolve the alert if it was raised
             */
            Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
            if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                
                if (event.getPreviousStatus() != null && event.getPreviousStatus().equals(MemberAliveIndicatorStatus.SUSPECTING_FAILURE)) {
                    factory.description("Suspecting failure of " + event.getProcessingUnitInstance().getProcessingUnitInstanceName());
                    factory.severity(AlertSeverity.WARNING);
                } else {
                    factory.description("Detected failure of " + event.getProcessingUnitInstance().getProcessingUnitInstanceName());
                    factory.severity(AlertSeverity.SEVERE);
                }
                
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(event.getProcessingUnitInstance().getUid());
                factory.componentDescription(event.getProcessingUnitInstance().getProcessingUnitInstanceName());
                factory.config(config.getProperties());
                
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.PROCESSING_UNIT_NAME, event.getProcessingUnitInstance().getProcessingUnit().getName());
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.MEMBER_ALIVE_INDICATOR_STATUS, event.getNewStatus().name());
                
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.HOST_ADDRESS, event.getProcessingUnitInstance().getMachine().getHostAddress());
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.HOST_NAME, event.getProcessingUnitInstance().getMachine().getHostName());
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.VIRTUAL_MACHINE_UID, event.getProcessingUnitInstance().getVirtualMachine().getUid());
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.CPU_UTILIZATION, String.valueOf(event.getProcessingUnitInstance().getOperatingSystem().getStatistics().getCpuPerc()*100.0));
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.HEAP_UTILIZATION, String.valueOf(event.getProcessingUnitInstance().getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
                
                removeProcessingUnitInstanceFromMap(event);
                    
                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert( new ProcessingUnitInstanceMemberAliveIndicatorAlert(alert));
            }
        } else {
            /*
             * raise suspecting or failure alerts
             */
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            
            if (event.getNewStatus().equals(MemberAliveIndicatorStatus.SUSPECTING_FAILURE)) {
                factory.description("Suspecting failure of " + event.getProcessingUnitInstance().getProcessingUnitInstanceName());
                factory.severity(AlertSeverity.WARNING);
            } else if (event.getNewStatus().equals(MemberAliveIndicatorStatus.DETECTED_FAILURE)) {
                factory.description("Detected failure of " + event.getProcessingUnitInstance().getProcessingUnitInstanceName());
                factory.severity(AlertSeverity.SEVERE);
            }
            
            factory.status(AlertStatus.RAISED);
            factory.componentUid(event.getProcessingUnitInstance().getUid());
            factory.componentDescription(event.getProcessingUnitInstance().getProcessingUnitInstanceName());
            factory.config(config.getProperties());
            
            factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.PROCESSING_UNIT_NAME, event.getProcessingUnitInstance().getProcessingUnit().getName());
            factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.MEMBER_ALIVE_INDICATOR_STATUS, event.getNewStatus().name());
            
            factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.HOST_ADDRESS, event.getProcessingUnitInstance().getMachine().getHostAddress());
            factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.HOST_NAME, event.getProcessingUnitInstance().getMachine().getHostName());
            factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.VIRTUAL_MACHINE_UID, event.getProcessingUnitInstance().getVirtualMachine().getUid());
            factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.CPU_UTILIZATION, String.valueOf(event.getProcessingUnitInstance().getOperatingSystem().getStatistics().getCpuPerc()*100.0));
            factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.HEAP_UTILIZATION, String.valueOf(event.getProcessingUnitInstance().getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
            
            addProcessingUnitInstanceToMap(event);
            
            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new ProcessingUnitInstanceMemberAliveIndicatorAlert(alert));
        }
    }

    private void removeProcessingUnitInstanceFromMap(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent event) {
        String key = event.getProcessingUnitInstance().getProcessingUnit().getName();
        List<ProcessingUnitInstance> listOfProcessingUnitInstances = mapProcessingUnitNameToProcessingUnitInstances.get(key);
        if (listOfProcessingUnitInstances != null) {
            listOfProcessingUnitInstances.remove(event.getProcessingUnitInstance());
        }
    }

    /**
     * Keep a map of processing unit name -> processing unit instances so we can 'resolve' the alert on processing unit removal.
     */
    private void addProcessingUnitInstanceToMap(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent event) {
        String key = event.getProcessingUnitInstance().getProcessingUnit().getName();
        List<ProcessingUnitInstance> listOfProcessingUnitInstances = mapProcessingUnitNameToProcessingUnitInstances.get(key);
        if (listOfProcessingUnitInstances == null) {
            listOfProcessingUnitInstances = new ArrayList<ProcessingUnitInstance>();
            listOfProcessingUnitInstances.add(event.getProcessingUnitInstance());
            mapProcessingUnitNameToProcessingUnitInstances.put(key, listOfProcessingUnitInstances);
        } else {
            if (!listOfProcessingUnitInstances.contains(event.getProcessingUnitInstance())) {
                listOfProcessingUnitInstances.add(event.getProcessingUnitInstance());
            }
        }
    }
    
    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }
}
