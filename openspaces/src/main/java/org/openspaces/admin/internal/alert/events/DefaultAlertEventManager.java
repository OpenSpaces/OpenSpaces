/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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
