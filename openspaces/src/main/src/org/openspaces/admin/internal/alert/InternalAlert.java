package org.openspaces.admin.internal.alert;

import org.openspaces.admin.alert.Alert;

public interface InternalAlert extends Alert {
    
    void setAlertUid(String alertUid);
}
