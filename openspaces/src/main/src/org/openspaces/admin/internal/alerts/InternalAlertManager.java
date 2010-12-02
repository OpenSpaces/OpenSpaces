package org.openspaces.admin.internal.alerts;

import org.openspaces.admin.alerts.AlertManager;

public interface InternalAlertManager extends AlertManager {
	
	AlertRepository getAlertRepository();
}
