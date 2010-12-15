package org.openspaces.admin.internal.alerts;

import org.openspaces.admin.alerts.Alert;

public interface AlertRepository extends Iterable<Alert> {
	
    void addAlert(Alert alert);
    
    void removeAlert(Alert alert);
    
    void setAlertsHistorySize(int historySize);

    /** get a specific alert by the unique alert identifier */
	Alert getAlertByUid(String alertUid);
	
	/** get all alerts belonging to this unique group identifier */
	Alerts getAlertsByGroupUid(String groupUid);
	
}
