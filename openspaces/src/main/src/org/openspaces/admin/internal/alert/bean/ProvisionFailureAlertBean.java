package org.openspaces.admin.internal.alert.bean;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.ProvisionFailureAlert;
import org.openspaces.admin.alert.config.ProvisionFailureAlertConfiguration;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.internal.alert.bean.util.AlertBeanUtils;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventListener;

public class ProvisionFailureAlertBean implements AlertBean, ProcessingUnitStatusChangedEventListener, ProcessingUnitRemovedEventListener {

    public static final String beanUID = "7d04ff97-6d49b2fc-e1f2-4805-add9-a0885a389994";
    public static final String ALERT_NAME = "Provision Failure";
    private Admin admin;
    private final ProvisionFailureAlertConfiguration config = new ProvisionFailureAlertConfiguration();
    
    public void afterPropertiesSet() throws Exception {
        admin.getProcessingUnits().getProcessingUnitStatusChanged().add(this);
    }

    public void destroy() throws Exception {
        admin.getProcessingUnits().getProcessingUnitStatusChanged().remove(this);
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
    

    public void processingUnitRemoved(ProcessingUnit processingUnit) {
        handleProcessingUnitEvent(processingUnit);
    }

    public void processingUnitStatusChanged(ProcessingUnitStatusChangedEvent event) {
        ProcessingUnit processingUnit = event.getProcessingUnit();

        //is being deployed? moving from NA-->Broken
        if (event.getPreviousStatus().equals(DeploymentStatus.NA) && event.getNewStatus().equals(DeploymentStatus.BROKEN)) {
            return;
        }
        
        handleProcessingUnitEvent(processingUnit);
    }
    
    /**
     * Handling of processing unit event - alert when planned > actual (less actual instances than planned).
     */
    private void handleProcessingUnitEvent(ProcessingUnit processingUnit) {
        final int planned = processingUnit.getTotalNumberOfInstances();
        final int actual = processingUnit.getInstances().length;
        final DeploymentStatus status = processingUnit.getStatus();
                
        if (planned>actual && (status.equals(DeploymentStatus.BROKEN) || status.equals(DeploymentStatus.COMPROMISED))) {
            final String groupUid = generateGroupUid(processingUnit.getName());
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            if (status.equals(DeploymentStatus.BROKEN)) {
                factory.description("Processing unit " + processingUnit.getName() + " has zero instances running instead of " + planned);
            } else {
                factory.description("Processing unit " + processingUnit.getName() + " has less than " + planned + " instances running");
            }
            factory.severity(AlertSeverity.SEVERE);
            factory.status(AlertStatus.RAISED);
            factory.componentUid(processingUnit.getName());
            factory.componentDescription(AlertBeanUtils.getProcessingUnitDescription(processingUnit));
            factory.config(config.getProperties());

            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new ProvisionFailureAlert(alert));
        }
        else if ( (planned == actual && status.equals(DeploymentStatus.INTACT))
                  || ((actual == 0) && status.equals(DeploymentStatus.UNDEPLOYED)) ) {
            final String groupUid = generateGroupUid(processingUnit.getName());
            Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
            if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                if (status.equals(DeploymentStatus.UNDEPLOYED)) {
                    factory.description("Processing unit " + processingUnit.getName() + " undeployed");
                } else {
                    factory.description("Processing unit " + processingUnit.getName() + " has " + actual
                            + " instances running");
                }
                factory.severity(AlertSeverity.SEVERE);
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(processingUnit.getName());
                factory.componentDescription(AlertBeanUtils.getProcessingUnitDescription(processingUnit));
                factory.config(config.getProperties());

                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert( new ProvisionFailureAlert(alert));
            }
        } else if (actual>planned && (status.equals(DeploymentStatus.INTACT))) {
            final String groupUid = generateGroupUid(processingUnit.getName());
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description("Processing unit " + processingUnit.getName() + " has extra instances running instead of " + planned);
            factory.severity(AlertSeverity.WARNING);
            factory.status(AlertStatus.RAISED);
            factory.componentUid(processingUnit.getName());
            factory.componentDescription(AlertBeanUtils.getProcessingUnitDescription(processingUnit));
            factory.config(config.getProperties());

            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new ProvisionFailureAlert(alert));
        }
    }

    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }
}
