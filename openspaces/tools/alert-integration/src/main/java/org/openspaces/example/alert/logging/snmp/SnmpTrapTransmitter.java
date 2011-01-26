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
 */

public class SnmpTrapTransmitter {
	
	
    @PostConstruct
    public void construct() throws Exception {
    	//loadRunParams();
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

	private String alertFileFilter; //"notify-alerts.xml" 
    private String loggerName; 
    private String group; 
	
		        	
	
    @PreDestroy 
    public void destroy() throws Exception {
    	if (alertManager != null && atListener != null) {
    		alertManager.getAlertTriggered().remove(atListener);
    	}
		alertManager = null;
		atListener = null;
		logger = null;
    }     
	
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

	private AlertManager alertManager;
	private Log logger; 
	private AlertTriggeredEventListener atListener;
	
}

