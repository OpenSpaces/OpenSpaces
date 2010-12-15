package org.openspaces.admin.internal.alerts;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openspaces.admin.alerts.Alert;
import org.openspaces.admin.alerts.AlertSeverity;

public class DefaultAlerts implements Alerts {

    //end of list has last fired alert
	private final List<Alert> alertsList;
	
	public DefaultAlerts(Alert[] alerts) {
		alertsList = Arrays.asList(alerts);
	}
	
	public Iterator<Alert> iterator() {
		return alertsList.iterator();
	}

    public Alert[] getAlerts() {
        return alertsList.toArray(new Alert[alertsList.size()]);
    }

    public String getAlertName() {
        return alertsList.get(0).getName();
    }

    public AlertSeverity getAlertSeverity() {
        //a group is OK if it does not end with another alert-severity.
        return alertsList.get(alertsList.size()-1).getSeverity();
    }

    public String getGroupUid() {
        return alertsList.get(0).getGroupUid();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getAlertSeverity()).append(" | ").append(getAlertName()).append(" | ").append(getGroupUid()).append('\n');
        for (Alert alert : this) {
            sb.append(alert).append('\n');
        }
        return sb.toString();
    }
}
