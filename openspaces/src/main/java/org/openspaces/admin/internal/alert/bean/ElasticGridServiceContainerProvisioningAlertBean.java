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

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.alerts.ElasticGridServiceContainerProvisioningAlert;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningFailureEvent;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningFailureEventListener;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningProgressChangedEvent;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningProgressChangedEventListener;

/**
 * Raises an alert if an elastic processing unit deployment or scale has been affected by a failure to start a new grid service container.
 * @since 8.0.6
 * @author itaif
 */
public class ElasticGridServiceContainerProvisioningAlertBean 
    extends AbstractElasticProcessingUnitAlertBean 
    implements ElasticGridServiceContainerProvisioningFailureEventListener , ElasticGridServiceContainerProvisioningProgressChangedEventListener  {
    
    private static final AlertSeverity GSC_ALERT_SEVERITY = AlertSeverity.SEVERE;
    private static final String GSC_ALERT_RESOLVED_DESCRIPTION_POSTFIX= "Grid Service Container provisioning for %s completed succesfully";
    private static final String GSC_ALERT_BEAN_UID = "adfd6015-2b91-4878-afef-65b91385a343";
    private static final String GSC_ALERT_NAME = "Grid Service Container Provisioning Alert";
    
    @Override
    public void afterPropertiesSet() throws Exception {
        super.setBeanUid(GSC_ALERT_BEAN_UID);
        super.setAlertName(GSC_ALERT_NAME);
        super.setAlertSeverity(GSC_ALERT_SEVERITY);
        super.setResolvedAlertDescriptionFormat(GSC_ALERT_RESOLVED_DESCRIPTION_POSTFIX);
        super.afterPropertiesSet();
        
        admin.getGridServiceContainers().getElasticGridServiceContainerProvisioningProgressChanged().add(this, true);
        admin.getGridServiceContainers().getElasticGridServiceContainerProvisioningFailure().add(this);   
    }

    @Override
    public void destroy() throws Exception {
        admin.getGridServiceContainers().getElasticGridServiceContainerProvisioningFailure().remove(this);
        admin.getGridServiceContainers().getElasticGridServiceContainerProvisioningProgressChanged().remove(this);
        super.destroy();
    }

    /**
     * Raises an alert when PU gsc provisioning is still in progress and 
     * this or other pu sharing the same GSC has encountered a gsc provisioning failure.
     * Note: As of 8.0.6 GSC sharing between Elastic PUs is not supported. 
     */
    @Override
    public void elasticGridServiceContainerProvisioningFailure(ElasticGridServiceContainerProvisioningFailureEvent event) {
        ElasticGridServiceContainerProvisioningAlert alert = new ElasticGridServiceContainerProvisioningAlert(createRaisedAlert(event));
        super.raiseAlert(alert);
    }

    /**
     * Resolves an alert when PU has the GSAs it needs.
     * This includes when a processing unit has been undeployed and all GSAs have been shutdown or not used by the PU.
     */
    @Override
    public void elasticGridServiceContainerProvisioningProgressChanged(ElasticGridServiceContainerProvisioningProgressChangedEvent event) {
        for (Alert baseAlert : createResolvedAlerts(event)) {
            ElasticGridServiceContainerProvisioningAlert alert = new ElasticGridServiceContainerProvisioningAlert(baseAlert);
            super.raiseAlert(alert);
        }
    }
}
