package org.openspaces.admin.internal.alerts;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openspaces.admin.alerts.Alert;
import org.openspaces.admin.alerts.AlertSeverity;

public class DefaultAlertHistory implements AlertHistory {

    //end of list has last fired alert
	private final List<Alert> alertHistory;
    private final DefaultAlertHistoryDetails details;
	
	public DefaultAlertHistory(Alert[] alerts) {
		alertHistory = Arrays.asList(alerts);
		details = new DefaultAlertHistoryDetails();
	}
	
	public Iterator<Alert> iterator() {
		return alertHistory.iterator();
	}

    public Alert[] getAlerts() {
        return alertHistory.toArray(new Alert[alertHistory.size()]);
    }

    public AlertHistoryDetails getDetails() {
        if (alertHistory.isEmpty()) return null;
        return details;
    }
    
    @Override
    public String toString() {
        AlertHistoryDetails details = getDetails();
        if (details == null) return "null";
        
        StringBuilder sb = new StringBuilder();
        sb.append(alertHistory.get(alertHistory.size()-1).getSeverity()).append(" | ").append(details.getName()).append(" | ").append(
                details.getGroupUid()).append('\n');
        for (Alert alert : this) {
            sb.append(alert).append('\n');
        }
        return sb.toString();
    }
    
    private class DefaultAlertHistoryDetails implements AlertHistoryDetails {

        public String getName() {
            return alertHistory.get(0).getName();
        }

        public boolean isResolved() {
            return AlertSeverity.OK.equals(alertHistory.get(alertHistory.size() -1).getSeverity());
        }

        public String getGroupUid() {
            return alertHistory.get(0).getGroupUid();
        }
    }
}
