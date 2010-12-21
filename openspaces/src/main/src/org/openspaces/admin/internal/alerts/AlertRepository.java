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
     * Set the amount of alerts to store for each aggregated group of alerts. Default is 200. This
     * does not include the first alert triggered, not the last alert to resolve the group.
     * 
     * @param groupHistorySize
     *            history size of alerts belonging to the same group.
     */
    void setGroupAlertHistorySize(int groupHistorySize);

    /**
     * Set the amount of resolved alerts to store. Default is 100. If the number of resolved alerts
     * exceeds the history size, the first alert to be resolved (by it's timestamp) is removed (FIFO
     * order).
     * 
     * @param resolvedHistorySize
     *            history size of resolved alerts.
     */
    void setResolvedAlertHistorySize(int resolvedHistorySize);
    
    /** get a specific alert by the unique alert identifier */
	Alert getAlertByUid(String alertUid);
	
	/** get all alerts belonging to this unique group identifier */
	AlertHistory getAlertHistoryByGroupUid(String groupUid);
	
	/** get all alerts aggregated by their group UID */
	AlertHistory[] getAlertHistory();
}
