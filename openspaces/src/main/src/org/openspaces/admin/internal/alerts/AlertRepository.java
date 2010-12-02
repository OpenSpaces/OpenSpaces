package org.openspaces.admin.internal.alerts;

import org.openspaces.admin.alerts.Alert;

public interface AlertRepository extends Iterable<Alert> {
	
	Alert getAlertById(String id);
	Alert[] getAlertsByType(String type);
	AlertGroup[] getAlertsGroupedByType();
	
	void addAlert(Alert alert);
	void removeAlert(Alert alert);
	
	void setAlertsHistorySize(int historySize);
}
