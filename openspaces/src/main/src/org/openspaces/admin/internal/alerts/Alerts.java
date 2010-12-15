package org.openspaces.admin.internal.alerts;

import org.openspaces.admin.alerts.Alert;
import org.openspaces.admin.alerts.AlertSeverity;

public interface Alerts extends Iterable<Alert> {
    
    /** get all the alerts represented by this alert aggregation */
    Alert[] getAlerts();
    
    /** get the name of the alert represented by this alert aggregation */
    String  getAlertName();
    /** get the unique group identification of the alert represented by this alert aggregation */
    String  getGroupUid();

    /**
     * get the last alert severity represented by this alert aggregation. A severity of
     * {@link AlertSeverity#OK} indicates that the alert has been resolved.
     */
    AlertSeverity getAlertSeverity();
}
