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
public class DefaultGatewayProcessingUnitRemovedEventManager implements InternalGatewayProcessingUnitRemovedEventManager {

    private final GatewayProcessingUnits gatewayProcessingUnits;

    private final InternalAdmin admin;

    private final List<GatewayProcessingUnitRemovedEventListener> listeners = 
    						new CopyOnWriteArrayList<GatewayProcessingUnitRemovedEventListener>();

    public DefaultGatewayProcessingUnitRemovedEventManager(GatewayProcessingUnits processingUnits) {
        this.gatewayProcessingUnits = processingUnits;
        this.admin = (InternalAdmin) processingUnits.getAdmin();
    }

    @Override
    public void gatewayProcessingUnitRemoved(final GatewayProcessingUnit gatewayProcessingUnit) {
        for (final GatewayProcessingUnitRemovedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
            	@Override
                public void run() {
                    listener.gatewayProcessingUnitRemoved( gatewayProcessingUnit );
                }
            });
        }
    }

    @Override
    public void add(GatewayProcessingUnitRemovedEventListener eventListener) {
        listeners.add(eventListener);
    }

    @Override
    public void remove(GatewayProcessingUnitRemovedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureGatewayProcessingUnitRemovedEventListener(eventListener));
        } else {
            add((GatewayProcessingUnitRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureGatewayProcessingUnitRemovedEventListener(eventListener));
        } else {
            remove((GatewayProcessingUnitRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}