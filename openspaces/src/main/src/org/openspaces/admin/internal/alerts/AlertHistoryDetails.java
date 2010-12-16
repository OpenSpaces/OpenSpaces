package org.openspaces.admin.internal.alerts;

import org.openspaces.admin.alerts.AlertSeverity;

/**
 * History details of all the alerts aggregated by the same group UID (Alert{@link #getGroupUid()}.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public interface AlertHistoryDetails {
    
    /**
     * All alerts aggregated by the same group UID have the same name (Alert{@link #getName()}.
     * @return the name of the alert represented by this alert aggregation. 
     * */
    String  getName();
    
    /**
     * All alerts are aggregated by the same group UID (Alert{@link #getGroupUid()}.
     * @return the unique group identification of the alert represented by this alert aggregation. 
     */
    String  getGroupUid();

    /**
     * @return <code>true</code> if this alert was resolved (with a severity of {@link AlertSeverity#OK}).
     */
    boolean isResolved();
}
