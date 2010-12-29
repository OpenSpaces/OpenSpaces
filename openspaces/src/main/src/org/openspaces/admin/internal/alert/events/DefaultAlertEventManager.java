package org.openspaces.admin.internal.alert.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.events.AlertEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.alert.InternalAlertManager;

public class DefaultAlertEventManager implements InternalAlertEventManager {

    private final InternalAlertManager alerts;
    private final InternalAdmin admin;

    private final List<AlertEventListener> listeners = new CopyOnWriteArrayList<AlertEventListener>();

    public DefaultAlertEventManager(InternalAlertManager alerts) {
        this.alerts = alerts;
        this.admin = (InternalAdmin) alerts.getAdmin();
    }

    public void add(AlertEventListener eventListener) {
        add(eventListener, true);
    }

    public void add(final AlertEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                public void run() {
                    for (Alert alert : alerts.getAlertRepository()) {
                        eventListener.onAlert(alert);
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    public void remove(AlertEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void onAlert(final Alert alert) {
        for (final AlertEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.onAlert(alert);
                }
            });
        }
    }
}
