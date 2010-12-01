package org.openspaces.admin.alerts.events;

public interface AlertEventManager {
	void add(AlertEventListener listener);
	void add(AlertEventListener listener, boolean includeExisting);
	void remove(AlertEventListener listener);
}
