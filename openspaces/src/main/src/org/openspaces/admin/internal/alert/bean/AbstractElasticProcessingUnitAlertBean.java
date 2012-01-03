package org.openspaces.admin.internal.alert.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.internal.alert.InternalAlertManager;

public abstract class AbstractElasticProcessingUnitAlertBean implements AlertBean {

    private final Log logger;
    
    private String beanUid;
    private String alertName;
    private AlertSeverity alertSeverity;
    private String resolvedAlertDescriptionFormat;

    public AbstractElasticProcessingUnitAlertBean() {
        logger = LogFactory.getLog(this.getClass());
    }
    
    public void setBeanUid(String beanUid) {
        this.beanUid = beanUid;
    }

    public void setAlertSeverity(AlertSeverity alertSeverity) {
        this.alertSeverity = alertSeverity;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public void setResolvedAlertDescriptionFormat(String resolvedAlertDescriptionFormat) {
        this.resolvedAlertDescriptionFormat = resolvedAlertDescriptionFormat;
    }

    Admin admin;
    Map<String,String> properties;
    
    @Override
    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(this.getClass()+ " started");
        }
    }

    @Override
    public void destroy() throws Exception {
        
    }
    
    protected Alert[] createResolvedAlerts(String processingUnitName) {
        List<Alert> alerts = new ArrayList<Alert>();
        String groupUid = generateGroupUid(processingUnitName);
        // there could be multiple alerts per PU
        Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
        for (Alert alert : alertsByGroupUid) {
            if (alert.getStatus().equals(AlertStatus.RAISED)) {
                alerts.add(createResolvedAlert(processingUnitName));
            }
        }
        return alerts.toArray(new Alert[alerts.size()]);
    }
    
    protected Alert createRaisedAlert(String processingUnitName, String alertDescription) {
        return createAlert(AlertStatus.RAISED, processingUnitName, alertDescription);
    }
    
    private Alert createResolvedAlert(String processingUnitName) {
        String description = String.format(resolvedAlertDescriptionFormat,processingUnitName);
        return createAlert(AlertStatus.RESOLVED, processingUnitName, description);
    }
    
    private String generateGroupUid(String puName) {
        return beanUid.concat("-").concat(puName);
    }
    
    private Alert createAlert(AlertStatus status, String processingUnitName, String alertDescription) {
        
        final String groupUid = generateGroupUid(processingUnitName);
        return  
            new AlertFactory()
            .name(alertName)
            .severity(alertSeverity)
            .description(alertDescription)
            .status(status)
            .componentUid(processingUnitName)
            .componentDescription(processingUnitName)
            .groupUid(groupUid)
            .create();
    }

    public void raiseAlert(Alert alert) {
        admin.getAlertManager().triggerAlert(alert);
    }

}
