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

package org.openspaces.admin.gsm;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventManager;
import org.openspaces.admin.gsm.events.GridServiceManagerLifecycleEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.space.SpaceDeployment;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Grid Service Containers hold all the different {@link GridServiceManager}s that are currently
 * discovered.
 *
 * <p>Provides simple means to get all the current managers, as well as as registering for
 * manager lifecycle (added and removed) events.
 *
 * <p>Also provides the ability to deploy a processing unit or a space (which is also a processing unit,
 * that simply just starts a space) on a randomly selected GSM (more control on which manager to deploy
 * can use {@link org.openspaces.admin.gsm.GridServiceManager#deploy(org.openspaces.admin.pu.ProcessingUnitDeployment)}.
 *
 * @author kimchy
 */
public interface GridServiceManagers extends AdminAware, Iterable<GridServiceManager> {

    /**
     * Returns all the currently discovered managers.
     */
    GridServiceManager[] getManagers();

    /**
     * Returns a manager based on its uid.
     *
     * @see GridServiceManager#getUid()
     */
    GridServiceManager getManagerByUID(String uid);

    /**
     * Returns a map of grid service manager with the key as the uid.
     */
    Map<String, GridServiceManager> getUids();

    /**
     * Returns the number of managers current discovered.
     */
    int getSize();

    /**
     * Returns <code>true</code> if there are no managers, <code>false</code> otherwise.
     */
    boolean isEmpty();

    /**
     * Waits indefinitely till the provided number of managers are up.
     *
     * @param numberOfGridServiceManagers The number of managers to wait for
     */
    boolean waitFor(int numberOfGridServiceManagers);

    /**
     * Waits for the given timeout (in time unit) till the provided number of managers are up.
     * Returns <code>true</code> if the required number of managers were discovered, <code>false</code>
     * if the timeout expired.
     *
     * @param numberOfGridServiceManagers The number of managers to wait for
     */
    boolean waitFor(int numberOfGridServiceManagers, long timeout, TimeUnit timeUnit);

    /**
     * Deploys a processing unit based on the processing unit deployment information on a random grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait and return the actual processing unit that can be used.
     */
    ProcessingUnit deploy(ProcessingUnitDeployment deployment);

    /**
     * Deploys a space based on the space deployment information on a random grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(SpaceDeployment deployment);

    /**
     * Returns the grid service manager added event manager allowing to add and remove
     * {@link org.openspaces.admin.gsm.events.GridServiceManagerAddedEventListener}s.
     */
    GridServiceManagerAddedEventManager getGridServiceManagerAdded();

    /**
     * Returns the grid service container added event manager allowing to add and remove
     * {@link org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventListener}s.
     */
    GridServiceManagerRemovedEventManager getGridServiceManagerRemoved();

    /**
     * Allows to add a {@link GridServiceManagerLifecycleEventListener}.
     */
    void addLifecycleListener(GridServiceManagerLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link GridServiceManagerLifecycleEventListener}.
     */
    void removeLifecycleListener(GridServiceManagerLifecycleEventListener eventListener);
}
