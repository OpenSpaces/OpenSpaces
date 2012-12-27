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

import org.openspaces.admin.gateway.GatewayProcessingUnit;
import org.openspaces.admin.gateway.GatewayProcessingUnits;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;

/**
 * @since 9.5
 * @author evgeny
 */
public class DefaultGatewayProcessingUnitAddedEventManager implements InternalGatewayProcessingUnitAddedEventManager {

    private final GatewayProcessingUnits gatewayProcessingUnits;

    private final InternalAdmin admin;

    private final List<GatewayProcessingUnitAddedEventListener> listeners = 
    						new CopyOnWriteArrayList<GatewayProcessingUnitAddedEventListener>();

    public DefaultGatewayProcessingUnitAddedEventManager(GatewayProcessingUnits gatewayProcessingUnits) {
        this.gatewayProcessingUnits = gatewayProcessingUnits;
        this.admin = (InternalAdmin) gatewayProcessingUnits.getAdmin();
    }

    @Override
    public void gatewayProcessingUnitAdded(final GatewayProcessingUnit gatewayProcessingUnit) {
        for (final GatewayProcessingUnitAddedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
            	@Override
                public void run() {
                    listener.gatewayProcessingUnitAdded( gatewayProcessingUnit );
                }
            });
        }
    }

    @Override
    public void add( final GatewayProcessingUnitAddedEventListener eventListener ) {
        add(eventListener, true);
    }
    
    @Override
    public void add(final GatewayProcessingUnitAddedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
            	@Override
                public void run() {
                    for( GatewayProcessingUnit gatewayProcessingUnit : gatewayProcessingUnits ) {
                        eventListener.gatewayProcessingUnitAdded(gatewayProcessingUnit);
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    @Override
    public void remove(GatewayProcessingUnitAddedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureGatewayProcessingUnitAddedEventListener(eventListener));
        } else {
            add((GatewayProcessingUnitAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureGatewayProcessingUnitAddedEventListener(eventListener));
        } else {
            remove((GatewayProcessingUnitAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
