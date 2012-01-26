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
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProvisionStatus;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventListener;

public class ProvisionFailureAlertBean implements AlertBean, ProcessingUnitStatusChangedEventListener, ProcessingUnitRemovedEventListener, ProcessingUnitInstanceProvisionStatusChangedEventListener {

    public static final String beanUID = "7d04ff97-6d49b2fc-e1f2-4805-add9-a0885a389994";
    public static final String ALERT_NAME = "Provision Failure";
    private Admin admin;
    private final ProvisionFailureAlertConfiguration config = new ProvisionFailureAlertConfiguration();
    
    @Override
    public void afterPropertiesSet() throws Exception {
        admin.getProcessingUnits().getProcessingUnitStatusChanged().add(this);
        admin.getProcessingUnits().getProcessingUnitRemoved().add(this);
    }

    @Override
    public void destroy() throws Exception {
        admin.getProcessingUnits().getProcessingUnitStatusChanged().remove(this);
        admin.getProcessingUnits().getProcessingUnitRemoved().remove(this);
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
    

    @Override
    public void processingUnitRemoved(ProcessingUnit processingUnit) {
        handleProcessingUnitEvent(processingUnit, DeploymentStatus.UNDEPLOYED, null);
    }

    @Override
    public void processingUnitStatusChanged(ProcessingUnitStatusChangedEvent event) {
        ProcessingUnit processingUnit = event.getProcessingUnit();

        if (DeploymentStatus.INTACT.equals(event.getNewStatus())) {
            handleProcessingUnitEvent(processingUnit, DeploymentStatus.INTACT, null);
        } else {
            if (isElasticProcessingUnit(processingUnit)) {
                return; //elastic PU will only trigger an alert for ProvisionStatus.FAILURE
            }
            
            //is being deployed? moving from null/NA-->Broken
            if ((event.getPreviousStatus() == null || event.getPreviousStatus().equals(DeploymentStatus.NA))
                    && event.getNewStatus().equals(DeploymentStatus.BROKEN)) {
                return;
            }
            
            handleProcessingUnitEvent(processingUnit, processingUnit.getStatus(), null);
        }
    }

    private boolean isElasticProcessingUnit(ProcessingUnit processingUnit) {
        Map<String, String> elasticProperties = ((InternalProcessingUnit)processingUnit).getElasticProperties();
        if (elasticProperties != null && !elasticProperties.isEmpty()) {
            return true;
        }
        return false;
    }
    
    @Override
    public void processingUnitInstanceProvisionStatusChanged(ProcessingUnitInstanceProvisionStatusChangedEvent event) {
        if (ProvisionStatus.FAILURE.equals(event.getNewStatus())) {
            handleProcessingUnitEvent(event.getProcessingUnit(), DeploymentStatus.COMPROMISED, event);
        }
    }
    
    /**
     * Handling of processing unit event - alert when planned > actual (less actual instances than planned).
     */
    private void handleProcessingUnitEvent(ProcessingUnit processingUnit, DeploymentStatus deploymentStatus, ProcessingUnitInstanceProvisionStatusChangedEvent event) {
        
        final String groupUid = generateGroupUid(processingUnit.getName());
        AlertFactory factory = new AlertFactory();
        factory.name(ALERT_NAME);
        factory.groupUid(groupUid);
        factory.componentUid(processingUnit.getName());
        factory.componentDescription(AlertBeanUtils.getProcessingUnitDescription(processingUnit));
        factory.config(config.getProperties());
        factory.severity(AlertSeverity.SEVERE);
        
        switch(deploymentStatus) {
        case BROKEN:
            factory.description("Processing unit " + processingUnit.getName() + " has zero instances running instead of " + processingUnit.getTotalNumberOfInstances());
            factory.status(AlertStatus.RAISED);
            break;
        case COMPROMISED:
            if (event != null) {
            factory.description("Processing unit " + processingUnit.getName() + " has encountered a failure instantiating " + event.getProcessingUnitInstanceName());
            } else {
                factory.description("Processing unit " + processingUnit.getName() + " has less than " + processingUnit.getTotalNumberOfInstances() + " instances running");
            }
            factory.status(AlertStatus.RAISED);
            break;
        case INTACT:
            factory.description("Processing unit " + processingUnit.getName() + " has instantiated successfully all " + processingUnit.getTotalNumberOfInstances() + " instances");
            factory.status(AlertStatus.RESOLVED);
            break;
        case UNDEPLOYED:
            factory.description("Processing unit " + processingUnit.getName() + " has been undeployed");
            factory.status(AlertStatus.RESOLVED);
            break;
        default:
            return; //don't trigger alert
        }
        
        Alert alert = factory.toAlert();
        
        boolean triggerAlert = false;
        if (alert.getStatus().isResolved()) {
            Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
            if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
                triggerAlert = true; //trigger a 'resolution' alert
            }
        } else {
            triggerAlert = true; //trigger an 'unresolved' alert
        }
        if (triggerAlert) {
            admin.getAlertManager().triggerAlert( new ProvisionFailureAlert(alert));
        }
    }

    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }
}
