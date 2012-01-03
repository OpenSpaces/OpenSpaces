package org.openspaces.admin.internal.alert.bean;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.ProcessingUnitInstanceMemberAliveIndicatorAlert;
import org.openspaces.admin.alert.config.ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.pu.MemberAliveIndicatorStatus;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener;

public class ProcessingUnitInstanceMemberAliveIndicatorAlertBean implements AlertBean, ProcessingUnitInstanceRemovedEventListener, ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener {

    public static final String beanUID = "c5a43e4-39eaf476-fd5d-4399-8b7b-a97c3ec4f49a";
    public static final String ALERT_NAME = "Member Alive Indicator";
    
    private final ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration config = new ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration();

    private Admin admin;

    public ProcessingUnitInstanceMemberAliveIndicatorAlertBean() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        validateProperties();
        admin.getProcessingUnits().getProcessingUnitInstanceRemoved().add(this);
        admin.getProcessingUnits().getProcessingUnitInstanceMemberAliveIndicatorStatusChanged().add(this);
    }

    @Override
    public void destroy() throws Exception {
        admin.getProcessingUnits().getProcessingUnitInstanceRemoved().remove(this);
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
    public void processingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance) {

        final String groupUid = generateGroupUid(processingUnitInstance.getProcessingUnitInstanceName());

        Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
        if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description("Processing Unit " + processingUnitInstance.getProcessingUnit().getName() + " has been removed.");
            factory.severity(AlertSeverity.INFO);
            
            factory.status(AlertStatus.NA);
            factory.componentUid(processingUnitInstance.getUid());
            factory.componentDescription(processingUnitInstance.getProcessingUnitInstanceName());
            factory.config(config.getProperties());

            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new ProcessingUnitInstanceMemberAliveIndicatorAlert(alert));
        }        
    }
    
    @Override
    public void processingUnitInstanceMemberAliveIndicatorStatusChanged(
            ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent event) {

        final String groupUid = generateGroupUid(event.getProcessingUnitInstance().getProcessingUnitInstanceName());

        if (event.getNewStatus().equals(MemberAliveIndicatorStatus.ALIVE)) {
            /*
             * resolve the alert if it was raised
             */
            Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
            if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description("Suspecting failure of " + event.getProcessingUnitInstance().getProcessingUnitInstanceName());
                
                if (event.getPreviousStatus() != null && event.getPreviousStatus().equals(MemberAliveIndicatorStatus.SUSPECTING_FAILURE)) {
                    factory.severity(AlertSeverity.WARNING);
                } else {
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
            factory.description("Suspecting failure of " + event.getProcessingUnitInstance().getProcessingUnitInstanceName());
            
            if (event.getNewStatus().equals(MemberAliveIndicatorStatus.SUSPECTING_FAILURE)) {
                factory.severity(AlertSeverity.WARNING);
            } else if (event.getNewStatus().equals(MemberAliveIndicatorStatus.DETECTED_FAILURE)) {
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
            
            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new ProcessingUnitInstanceMemberAliveIndicatorAlert(alert));
        }
    }
    
    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }
}
