package org.openspaces.admin.alerts.events;

import org.openspaces.admin.alerts.Alert;

public interface AlertEventListener {
	public void onAlert(Alert alert);
}
