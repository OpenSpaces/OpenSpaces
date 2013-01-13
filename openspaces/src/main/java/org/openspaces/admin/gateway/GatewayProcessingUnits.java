/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.gateway;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.gateway.events.GatewayProcessingUnitAddedEventManager;
import org.openspaces.admin.gateway.events.GatewayProcessingUnitLifecycleEventListener;
import org.openspaces.admin.gateway.events.GatewayProcessingUnitRemovedEventManager;

/**
 * Holds one or more {@link org.openspaces.admin.gateway.GatewayProcessingUnit}s.
 *
 * @since 9.5
 * @author evgeny
 */
public interface GatewayProcessingUnits extends Iterable<GatewayProcessingUnit>, AdminAware{

    /**
     * Returns the number of currently deployed {@link org.openspaces.admin.gateway.GatewayProcessingUnit}s.
     */
    int getSize();

    /**
     * Returns <code>true</code> if there are no currently deployed processing units.
     */
    boolean isEmpty();

    /**
     * Returns the {@link org.openspaces.admin.gateway.GatewayProcessingUnit}s currently deployed.
     */
    GatewayProcessingUnit[] getGatewayProcessingUnits();

    /**
     * Returns the {@link org.openspaces.admin.gateway.GatewayProcessingUnit} for the given gateway processing unit name.
     */
    GatewayProcessingUnit getGatewayProcessingUnit(String uid);

    /**
     * Returns a map of {@link org.openspaces.admin.gateway.GatewayProcessingUnit} keyed by their respective names.
     */
    Map<String, GatewayProcessingUnit> getNames();

    /**
     * Waits indefinitely till the gateway processing unit is identified as deployed. Returns the
     * {@link org.openspaces.admin.gateway.GatewayProcessingUnit}.
     */
    GatewayProcessingUnit waitFor(String gatewayProcessingUnitName);

    /**
     * Waits for the specified timeout (in time interval) till the processing unit is identified as deployed. Returns the
     * {@link org.openspaces.admin.gateway.GatewayProcessingUnit}. Return <code>null</code> if the gateway processing unit is not deployed
     * within the specified timeout.
     */
    GatewayProcessingUnit waitFor(String gatewaysProcessingUnitName, long timeout, TimeUnit timeUnit);

    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.gateway.events.GatewayProcessingUnitAddedEventListener}s.
     */
    GatewayProcessingUnitAddedEventManager getGatewayProcessingUnitAdded();

    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.gateway.events.GatewayProcessingUnitRemovedEventListener}s.
     */
    GatewayProcessingUnitRemovedEventManager getGatewayProcessingUnitRemoved();

    /**
     * Allows to add a {@link GatewayProcessingUnitLifecycleEventListener}.
     */
    void addLifecycleListener(GatewayProcessingUnitLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link GatewayProcessingUnitLifecycleEventListener}.
     */
    void removeLifecycleListener(GatewayProcessingUnitLifecycleEventListener eventListener);
}