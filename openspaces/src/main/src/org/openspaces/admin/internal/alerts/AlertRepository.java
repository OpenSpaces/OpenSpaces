package org.openspaces.admin.internal.alerts;

import org.openspaces.admin.alerts.Alert;

public interface AlertRepository extends Iterable<Alert> {
	
    /**
     * Add the alert to the repository unless the alert is 'resolved' and there is no matching 'unresolved' alert.
     * The added alert will be set with an alert UID.
     * @param alert the alert to add to the repository.
     * @return <code>true</code> if the alert was added; <code>false</code> if alert was discarded.
     */
    boolean addAlert(Alert alert);
    
    /**
     * Remove the alert history from the repository. Usually called if the component is no longer reachable.
     * @param groupUid the group UID aggregating the alerts.
     * @return <code>true</code> if alert history for this group UID has been removed; <code>false</code> otherwise.
     */
    boolean removeAlertHistoryByGroupUid(String groupUid);
    
    /**
     * Set the amount of alerts to store for each aggregated group of alerts. Default is 200.
     * @param historySize history size of un-resolved alerts per group UID.
     */
    void setAlertsHistorySize(int historySize);
    
    /** get a specific alert by the unique alert identifier */
	Alert getAlertByUid(String alertUid);
	
	/** get all alerts belonging to this unique group identifier */
	AlertHistory getAlertHistoryByGroupUid(String groupUid);
	
	/** get all alerts aggregated by their group UID */
	AlertHistory[] getAlertHistory();
}
