package org.openspaces.admin.internal.alert;

import org.openspaces.admin.alert.Alert;

public interface AlertHistory extends Iterable<Alert> {

    /**
     * @return all the alerts represented by this alert aggregation; zero length array if there is
     *         no history.
     */
    Alert[] getAlerts();

    /**
     * @return the details representing this alert aggregation; <code>null</code> if there is no
     *         history.
     */
    AlertHistoryDetails getDetails();
}
