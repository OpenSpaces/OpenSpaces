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
import org.openspaces.admin.alert.alerts.ElasticAutoScalingAlert;
import org.openspaces.admin.machine.events.ElasticAutoScalingFailureEvent;
import org.openspaces.admin.machine.events.ElasticAutoScalingFailureEventListener;
import org.openspaces.admin.machine.events.ElasticAutoScalingProgressChangedEvent;
import org.openspaces.admin.machine.events.ElasticAutoScalingProgressChangedEventListener;

/**
 * Raises an alert if an elastic processing unit deployment or scale has been affected by a failure to start a (cloud) virtual machine.
 * @since 8.0.6
 * @author itaif
 */
public class ElasticAutoScalingAlertBean extends AbstractElasticProcessingUnitAlertBean implements 
    ElasticAutoScalingFailureEventListener, ElasticAutoScalingProgressChangedEventListener {
    
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
     
        admin.getProcessingUnits().getElasticAutoScalingProgressChanged().add(this, true);
        admin.getProcessingUnits().getElasticAutoScalingFailure().add(this);   
    }

    @Override
    public void destroy() throws Exception {
        admin.getProcessingUnits().getElasticAutoScalingFailure().remove(this);
        admin.getProcessingUnits().getElasticAutoScalingProgressChanged().remove(this);
        super.destroy();
    }

    /**
     * Raises an alert when PU machine provisioning is still in progress and 
     * this or other pu sharing the same machine has encountered a machine provisioning failure.
     */
    @Override
    public void elasticAutoScalingFailure(ElasticAutoScalingFailureEvent event) {
        String description = event.getFailureDescription();
        for (String puName : event.getProcessingUnitNames()) {
            ElasticAutoScalingAlert alert = new ElasticAutoScalingAlert(createRaisedAlert(puName, description));
            super.raiseAlert(alert);
        }
    }

    /**
     * Resolves an alert when PU has the machines it needs.
     * This includes when a processing unit has been undeployed and all machines have been shutdown or not used by the PU.
     */
    @Override
    public void elasticAutoScalingProgressChanged(ElasticAutoScalingProgressChangedEvent event) {
        String puName = event.getProcessingUnitName();
        if (event.isComplete()) {
            for (Alert baseAlert : createResolvedAlerts(puName)) {
                ElasticAutoScalingAlert alert = new ElasticAutoScalingAlert(baseAlert);
                super.raiseAlert(alert);
            }
        }
    }
}
