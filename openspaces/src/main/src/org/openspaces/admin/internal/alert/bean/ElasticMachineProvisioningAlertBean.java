package org.openspaces.admin.internal.alert.bean;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.alerts.ElasticMachineProvisioningAlert;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningFailureEvent;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningFailureEventListener;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEvent;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEventListener;

/**
 * Raises an alert if an elastic processing unit deployment or scale has been affected by a failure to start a (cloud) virtual machine.
 * @since 8.0.6
 * @author itaif
 */
public class ElasticMachineProvisioningAlertBean extends AbstractElasticProcessingUnitAlertBean implements 
    ElasticMachineProvisioningFailureEventListener, ElasticMachineProvisioningProgressChangedEventListener {
    
    private static final String MACHINES_ALERT_BEAN_UID = "3BA87E89-449A-4abc-A632-4732246A9EE4";
    private static final String MACHINES_ALERT_NAME = "Machine Provisioning Alert";
    private static final AlertSeverity MACHINES_ALERT_SEVERITY = AlertSeverity.SEVERE;
    private static final String MACHINES_ALERT_RESOLVED_DESCRIPTION_POSTFIX= "Machine Provisioning for %s completed succesfully";
        
    @Override
    public void afterPropertiesSet() throws Exception {
        super.setBeanUid(MACHINES_ALERT_BEAN_UID);
        super.setAlertName(MACHINES_ALERT_NAME);
        super.setAlertSeverity(MACHINES_ALERT_SEVERITY);
        super.setResolvedAlertDescriptionFormat(MACHINES_ALERT_RESOLVED_DESCRIPTION_POSTFIX);
        super.afterPropertiesSet();
     
        admin.getMachines().getElasticMachineProvisioningProgressChanged().add(this, true);
        admin.getMachines().getElasticMachineProvisioningFailure().add(this);   
    }

    @Override
    public void destroy() throws Exception {
        admin.getMachines().getElasticMachineProvisioningFailure().remove(this);
        admin.getMachines().getElasticMachineProvisioningProgressChanged().remove(this);
        super.destroy();
    }

    /**
     * Raises an alert when PU machine provisioning is still in progress and 
     * this or other pu sharing the same machine has encountered a machine provisioning failure.
     */
    @Override
    public void elasticMachineProvisioningFailure(ElasticMachineProvisioningFailureEvent event) {
        String description = event.getFailureDescription();
        for (String puName : event.getProcessingUnitNames()) {
            admin.getAlertManager().triggerAlert(createRaisedAlert(puName, description));
        }
    }

    /**
     * Resolves an alert when PU has the machines it needs.
     * This includes when a processing unit has been undeployed and all machines have been shutdown or not used by the PU.
     */
    @Override
    public void elasticMachineProvisioningProgressChanged(ElasticMachineProvisioningProgressChangedEvent event) {
        String puName = event.getProcessingUnitName();
        if (event.isComplete()) {
            for (Alert baseAlert : createResolvedAlerts(puName)) {
                ElasticMachineProvisioningAlert alert = new ElasticMachineProvisioningAlert(baseAlert);
                admin.getAlertManager().triggerAlert(alert);
            }
        }
    }
}