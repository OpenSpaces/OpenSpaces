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
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;

public class ProcessingUnitInstanceMemberAliveIndicatorAlertBean implements AlertBean,
        ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener, ProcessingUnitRemovedEventListener,
        ProcessingUnitInstanceAddedEventListener {

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
        admin.getProcessingUnits().getProcessingUnitInstanceAdded().add(this);
        admin.getProcessingUnits().getProcessingUnitInstanceMemberAliveIndicatorStatusChanged().add(this);
    }

    @Override
    public void destroy() throws Exception {
        admin.getProcessingUnits().getProcessingUnitRemoved().remove(this);
        admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(this);
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
                    factory.status(AlertStatus.RESOLVED);
                } else {
                    factory.description("Processing Unit " + processingUnitInstance.getProcessingUnit().getName() + " has been removed");
                    factory.severity(AlertSeverity.SEVERE);
                    factory.status(AlertStatus.NA);
                }

                factory.componentUid(processingUnitInstance.getUid());
                factory.componentDescription(processingUnitInstance.getProcessingUnitInstanceName());
                factory.config(config.getProperties());
                
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.PROCESSING_UNIT_NAME, processingUnitInstance.getProcessingUnit().getName());
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.MEMBER_ALIVE_INDICATOR_STATUS, processingUnitInstance.getMemberAliveIndicatorStatus().name());
                
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.HOST_ADDRESS, processingUnitInstance.getMachine().getHostAddress());
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.HOST_NAME, processingUnitInstance.getMachine().getHostName());
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.VIRTUAL_MACHINE_UID, processingUnitInstance.getVirtualMachine().getUid());
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.CPU_UTILIZATION, String.valueOf(processingUnitInstance.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.HEAP_UTILIZATION, String.valueOf(processingUnitInstance.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));


                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert( new ProcessingUnitInstanceMemberAliveIndicatorAlert(alert));
            } 
        }

        mapProcessingUnitNameToProcessingUnitInstances.remove(processingUnit.getName());
        
    }
    
    @Override
    public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstanceAdded) {
        List<ProcessingUnitInstance> listOfProcessingUnitInstances = mapProcessingUnitNameToProcessingUnitInstances.get(processingUnitInstanceAdded.getProcessingUnit().getName());
        if (listOfProcessingUnitInstances == null) {
            return;
        }
        for (int i=0; i<listOfProcessingUnitInstances.size(); ++i) {
            ProcessingUnitInstance processingUnitInstance = listOfProcessingUnitInstances.get(i);

            //find same processing unit ref. and same processing unit instance name (different ref. since loaded elsewhere) 
            if (!(processingUnitInstanceAdded.getProcessingUnit().equals(processingUnitInstance.getProcessingUnit()) && processingUnitInstanceAdded.getProcessingUnitInstanceName()
                .equals(processingUnitInstance.getProcessingUnitInstanceName()))) {
                continue;
            } else {
                listOfProcessingUnitInstances.remove(i);
            }
            
            final String prevGroupUid = generateGroupUid(processingUnitInstance.getUid());

            Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(prevGroupUid);
            if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(prevGroupUid);
                
                factory.description("Re-provisioned " + processingUnitInstanceAdded.getProcessingUnitInstanceName() + " instance");
                factory.severity(alertsByGroupUid[0].getSeverity());

                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(processingUnitInstanceAdded.getUid());
                factory.componentDescription(processingUnitInstanceAdded.getProcessingUnitInstanceName());
                factory.config(config.getProperties());

                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.PROCESSING_UNIT_NAME, processingUnitInstanceAdded.getProcessingUnit().getName());
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.MEMBER_ALIVE_INDICATOR_STATUS, processingUnitInstanceAdded.getMemberAliveIndicatorStatus().name());
                
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.HOST_ADDRESS, processingUnitInstanceAdded.getMachine().getHostAddress());
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.HOST_NAME, processingUnitInstanceAdded.getMachine().getHostName());
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.VIRTUAL_MACHINE_UID, processingUnitInstanceAdded.getVirtualMachine().getUid());
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.CPU_UTILIZATION, String.valueOf(processingUnitInstanceAdded.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
                factory.putProperty(ProcessingUnitInstanceMemberAliveIndicatorAlert.HEAP_UTILIZATION, String.valueOf(processingUnitInstanceAdded.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));

                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert( new ProcessingUnitInstanceMemberAliveIndicatorAlert(alert));
            }
        }
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
