/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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
import org.openspaces.admin.internal.zone.config.ZonesConfigUtils;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitFailureEvent;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitProgressChangedEvent;

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
    
    protected Alert[] createResolvedAlerts(ElasticProcessingUnitProgressChangedEvent event) {
        
        if (!event.isComplete()) {
            return new Alert[0];
        }
        
        List<Alert> alerts = new ArrayList<Alert>();
        String groupUid = generateGroupUid(event);
        // there could be multiple alerts per PU
        Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
        if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
            alerts.add(createAlert(AlertStatus.RESOLVED, event));
        }
        return alerts.toArray(new Alert[alerts.size()]);
    
    }
 
    protected Alert createRaisedAlert(ElasticProcessingUnitFailureEvent event) {
        return createAlert(AlertStatus.RAISED, event);
    }
    
    private String generateGroupUid(ElasticProcessingUnitEvent event) {
        String groupUid = beanUid.concat("-").concat(event.getProcessingUnitName());
        if (event.getGridServiceAgentZones() != null) {
            groupUid = groupUid.concat(ZonesConfigUtils.zonesToString(event.getGridServiceAgentZones()));
        }
        return groupUid;
    }
    
    private Alert createAlert(AlertStatus status, ElasticProcessingUnitEvent event) {
        
        String alertDescription;
        
        if (status.equals(AlertStatus.RESOLVED)) {
            alertDescription = String.format(resolvedAlertDescriptionFormat, event.getProcessingUnitName());
        }
        else {
            alertDescription = event.toString();
        }
        
        final String groupUid = generateGroupUid(event);
        
        String zonesDescription = event.getGridServiceAgentZones() != null ? " in zones " + event.getGridServiceAgentZones() : "";
        
        return  
            new AlertFactory()
            .name(alertName)
            .severity(alertSeverity)
            .description(alertDescription)
            .status(status)
            .componentUid(event.getProcessingUnitName())
            .componentDescription(event.getProcessingUnitName() + zonesDescription)
            .groupUid(groupUid)
            .create();
    }

    public void raiseAlert(Alert alert) {
        if (logger.isDebugEnabled()) {
            logger.debug("Triggering alert:" + alert.getDescription());
        }
        admin.getAlertManager().triggerAlert(alert);
    }

}
