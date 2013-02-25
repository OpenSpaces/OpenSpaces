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
import org.openspaces.admin.pu.elastic.events.ElasticAutoScalingFailureEvent;
import org.openspaces.admin.pu.elastic.events.ElasticAutoScalingFailureEventListener;
import org.openspaces.admin.pu.elastic.events.ElasticAutoScalingProgressChangedEvent;
import org.openspaces.admin.pu.elastic.events.ElasticAutoScalingProgressChangedEventListener;

/**
 * Raises an alert if an elastic processing unit deployment or scale has been affected by a failure to start a (cloud) virtual machine.
 * @since 8.0.6
 * @author itaif
 */
public class ElasticAutoScalingAlertBean extends AbstractElasticProcessingUnitAlertBean implements 
    ElasticAutoScalingFailureEventListener, ElasticAutoScalingProgressChangedEventListener {
    
    private static final String AUTO_SCALING_ALERT_BEAN_UID = "AF5874F0-78E1-11E1-8DA5-1FC84724019B";
    private static final String AUTO_SCALING_ALERT_NAME = "Automatic Scaling Alert";
    private static final AlertSeverity AUTO_SCALING_ALERT_SEVERITY = AlertSeverity.SEVERE;
    private static final String AUTO_SCALING_ALERT_RESOLVED_DESCRIPTION_POSTFIX= "Automatic Scaling for %s completed succesfully";
        
    @Override
    public void afterPropertiesSet() throws Exception {
        super.setBeanUid(AUTO_SCALING_ALERT_BEAN_UID);
        super.setAlertName(AUTO_SCALING_ALERT_NAME);
        super.setAlertSeverity(AUTO_SCALING_ALERT_SEVERITY);
        super.setResolvedAlertDescriptionFormat(AUTO_SCALING_ALERT_RESOLVED_DESCRIPTION_POSTFIX);
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
        ElasticAutoScalingAlert alert = new ElasticAutoScalingAlert(createRaisedAlert(event));
        super.raiseAlert(alert);
    }

    /**
     * Resolves an alert when PU has the machines it needs.
     * This includes when a processing unit has been undeployed and all machines have been shutdown or not used by the PU.
     */
    @Override
    public void elasticAutoScalingProgressChanged(ElasticAutoScalingProgressChangedEvent event) {
        if (event.isComplete()) {
	    	for (Alert baseAlert : createResolvedAlerts(event)) {
	            ElasticAutoScalingAlert alert = new ElasticAutoScalingAlert(baseAlert);
	            super.raiseAlert(alert);
	        }
        }
    }
}
