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

package org.openspaces.admin.esm;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;
import org.openspaces.admin.esm.events.ElasticServiceManagerAddedEventManager;
import org.openspaces.admin.esm.events.ElasticServiceManagerLifecycleEventListener;
import org.openspaces.admin.esm.events.ElasticServiceManagerRemovedEventManager;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * <p>Provides simple means to get all the current managers, as well as as registering for
 * manager lifecycle (added and removed) events.
 *
 * @author Moran Avigdor
 */
public interface ElasticServiceManagers extends AdminAware, Iterable<ElasticServiceManager>, DumpProvider {

    /**
     * Returns all the currently discovered managers.
     */
    ElasticServiceManager[] getManagers();

    /**
     * Returns a manager based on its uid.
     *
     * @see ElasticServiceManager#getUid()
     */
    ElasticServiceManager getManagerByUID(String uid);

    /**
     * Returns a map of elastic service manager with the key as the uid.
     */
    Map<String, ElasticServiceManager> getUids();

    /**
     * Returns the number of managers currently discovered.
     */
    int getSize();

    /**
     * Returns <code>true</code> if there are no managers, <code>false</code> otherwise.
     */
    boolean isEmpty();

    /**
     * Waits indefinitely till at least one ESM is discovered and returns it.
     */
    ElasticServiceManager waitForAtLeastOne();

    /**
     * Waits for the given timeout (in time unit) till at least one ESM is discovered and returns it.
     */
    ElasticServiceManager waitForAtLeastOne(long timeout, TimeUnit timeUnit);

    /**
     * Waits indefinitely till the provided number of managers are up. When passing 0, will wait
     * till there are no more managers.
     *
     * @param numberOfElasticServiceManagers The number of managers to wait for
     */
    boolean waitFor(int numberOfElasticServiceManagers);

    /**
     * Waits for the given timeout (in time unit) till the provided number of managers are up.
     * Returns <code>true</code> if the required number of managers were discovered, <code>false</code>
     * if the timeout expired.
     *
     * <p>When passing 0, will wait till there are no more managers.
     *
     * @param numberOfElasticServiceManagers The number of managers to wait for
     */
    boolean waitFor(int numberOfElasticServiceManagers, long timeout, TimeUnit timeUnit);

    
    /**
     * Deploys an 'elastic' deployment based on the deployment information and the available resources.
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     */
    ProcessingUnit deploy(ElasticDataGridDeployment deployment);
    
    /**
     * Deploys an 'elastic' deployment based on the deployment information and the available resources.
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     */
    ProcessingUnit deploy(ElasticDataGridDeployment deployment, long timeout, TimeUnit timeUnit);
    

    /**
     * Returns the grid service manager added event manager allowing to add and remove
     * {@link org.openspaces.admin.gsm.events.GridServiceManagerAddedEventListener}s.
     */
    ElasticServiceManagerAddedEventManager getElasticServiceManagerAdded();

    /**
     * Returns the grid service container added event manager allowing to add and remove
     * {@link org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventListener}s.
     */
    ElasticServiceManagerRemovedEventManager getElasticServiceManagerRemoved();

    /**
     * Allows to add a {@link ElasticServiceManagerLifecycleEventListener}.
     */
    void addLifecycleListener(ElasticServiceManagerLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link ElasticServiceManagerLifecycleEventListener}.
     */
    void removeLifecycleListener(ElasticServiceManagerLifecycleEventListener eventListener);
}
