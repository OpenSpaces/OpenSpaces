package org.openspaces.admin.internal.alert;

import org.openspaces.admin.alert.AlertManager;

public interface InternalAlertManager extends AlertManager {
	
	AlertRepository getAlertRepository();
}
