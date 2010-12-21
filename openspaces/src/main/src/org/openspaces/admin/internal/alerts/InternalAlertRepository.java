package org.openspaces.admin.internal.alerts;


public interface InternalAlertRepository extends AlertRepository {
    /**
     * Remove the alert history from the repository.
     * @param groupUid the group UID aggregating the alerts.
     * @return <code>true</code> if alert history for this group UID has been removed; <code>false</code> otherwise.
     */
    boolean removeAlertHistoryByGroupUid(String groupUid);

}
