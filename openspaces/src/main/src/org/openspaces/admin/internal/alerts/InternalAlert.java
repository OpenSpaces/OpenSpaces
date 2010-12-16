package org.openspaces.admin.internal.alerts;

import org.openspaces.admin.alerts.Alert;

public interface InternalAlert extends Alert {
    
    void setAlertUid(String alertUid);
}
