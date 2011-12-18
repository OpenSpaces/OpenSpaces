package org.openspaces.admin.internal.alert.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.internal.support.AlertsListenersRegistrationDelayAware;

public class DefaultAlertEventManager implements InternalAlertTriggeredEventManager {

    private final InternalAlertManager alerts;
    private final InternalAdmin admin;

    private final List<AlertTriggeredEventListener> listeners = new CopyOnWriteArrayList<AlertTriggeredEventListener>();
    
    public DefaultAlertEventManager(InternalAlertManager alerts) {
        this.alerts = alerts;
        this.admin = (InternalAdmin) alerts.getAdmin();
    }

    @Override
    public void add(AlertTriggeredEventListener eventListener) {
        add(eventListener, true);
    }

    @Override
    public void add(final AlertTriggeredEventListener eventListener, final boolean includeExisting) {
        
        if( eventListener instanceof AlertsListenersRegistrationDelayAware ){
            long alertsListenerRegistrationDelay = 
                ( ( AlertsListenersRegistrationDelayAware )eventListener ).getAlertRegistrationDelay();
            
            admin.scheduleOneTimeWithDelayNonBlockingStateChange( new Runnable() {
                
                @Override
                public void run() {

                    addListener( eventListener, includeExisting );                    
                }
            }, alertsListenerRegistrationDelay, TimeUnit.MILLISECONDS );
        }
        else{
            addListener( eventListener, includeExisting );
        }
    }

    private void addListener(final AlertTriggeredEventListener eventListener, boolean includeExisting) {

        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                @Override
                public void run() {
                    for (Alert alert : alerts.getAlertRepository().iterateFifo()) {
                        eventListener.alertTriggered(alert);
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    @Override
    public void remove(AlertTriggeredEventListener eventListener) {
        listeners.remove(eventListener);
    }

    @Override
    public void alertTriggered(final Alert alert) {
        for (final AlertTriggeredEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                @Override
                public void run() {
                    listener.alertTriggered(alert);
                }
            });
        }
    }
}
