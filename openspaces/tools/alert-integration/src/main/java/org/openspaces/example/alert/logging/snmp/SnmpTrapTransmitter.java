/*
 * Copyright 2011 GigaSpaces Technologies Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at *
 *     
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License."
 */
package org.openspaces.example.alert.logging.snmp;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.alert.config.parser.XmlAlertConfigurationParser;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;

/**
 *  SnmpAlertAgent: A stateless PU which intercepts XAP alerts to transmit them 
 *   to an SNMP software as asynchronous agent-generated messages ("traps")
 * 
 * @author giladh
 * @since 8.0
 */
public class SnmpTrapTransmitter {

	private String alertFileFilter; 
	private String loggerName; 
	private String group; 

	private AlertManager alertManager;
	private Log logger; 
	private AlertTriggeredEventListener atListener;	

	/**
	 * Construct SnmpTrapTransmitter bean object 
	 * @throws Exception
	 */
	@PostConstruct 
	public void construct() throws Exception {
		registerAlertTrapper();    	
	}

	public String getAlertFileFilter() {
		return alertFileFilter;
	}

	public void setAlertFileFilter(String alertFileFilter) {
		this.alertFileFilter = alertFileFilter;
	}

	public String getLoggerName() {
		return loggerName;
	}


	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}


	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Destroy SnmpTrapTransmitter bean object
	 * @throws Exception
	 */
	@PreDestroy 
	public void destroy() throws Exception {
		if (alertManager != null && atListener != null) {
			alertManager.getAlertTriggered().remove(atListener);
		}
		alertManager = null;
		atListener = null;
		logger = null;
	}     

	/**
	 * Register bean as an alert listener, using 
	 * alert filter file: alertFileFilter
	 */
	private void registerAlertTrapper() { 
		Admin admin = new AdminFactory().addGroup(group).create();         
		LogFactory.releaseAll(); 
		logger = LogFactory.getLog(loggerName); 

		alertManager = admin.getAlertManager();
		atListener = new AlertTriggeredEventListener() {
			public void alertTriggered(Alert alert) {
				String loggRecord;
				loggRecord = alert.toString();
				logger.info(loggRecord);
			}
		};	

		XmlAlertConfigurationParser cparse = new XmlAlertConfigurationParser(alertFileFilter);
		alertManager.configure(cparse.parse());        
		alertManager.getAlertTriggered().add(atListener);        		
	}
}

