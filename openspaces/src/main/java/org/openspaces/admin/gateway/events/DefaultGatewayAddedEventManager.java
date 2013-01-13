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
package org.openspaces.admin.gateway.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openspaces.admin.gateway.Gateway;
import org.openspaces.admin.gateway.Gateways;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;

/**
 * @since 9.5
 * @author evgeny
 */
public class DefaultGatewayAddedEventManager implements InternalGatewayAddedEventManager {

    private final Gateways gateways;

    private final InternalAdmin admin;

    private final List<GatewayAddedEventListener> listeners = 
    						new CopyOnWriteArrayList<GatewayAddedEventListener>();

    public DefaultGatewayAddedEventManager(Gateways gateways) {
        this.gateways = gateways;
        this.admin = (InternalAdmin)gateways.getAdmin();
    }

    @Override
    public void gatewayAdded(final Gateway gateway ) {
        for (final GatewayAddedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
            	@Override
                public void run() {
                    listener.gatewayAdded( gateway );
                }
            });
        }
    }

    @Override
    public void add( final GatewayAddedEventListener eventListener ) {
        add(eventListener, true);
    }
    
    @Override
    public void add(final GatewayAddedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
            	@Override
                public void run() {
                    for( Gateway gateway : gateways ) {
                        eventListener.gatewayAdded(gateway);
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    @Override
    public void remove(GatewayAddedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureGatewayAddedEventListener(eventListener));
        } else {
            add((GatewayAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureGatewayAddedEventListener(eventListener));
        } else {
            remove((GatewayAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
