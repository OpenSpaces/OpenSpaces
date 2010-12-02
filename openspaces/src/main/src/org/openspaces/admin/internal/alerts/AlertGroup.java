package org.openspaces.admin.internal.alerts;

import org.openspaces.admin.alerts.Alert;

public interface AlertGroup extends Iterable<Alert> {
	boolean isOpen();
	String getName();
}
