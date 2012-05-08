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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.ApplicationAlreadyDeployedException;
import org.openspaces.admin.application.ApplicationDeployment;
import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventManager;
import org.openspaces.admin.gsm.events.GridServiceManagerLifecycleEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventManager;
import org.openspaces.admin.memcached.MemcachedDeployment;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitAlreadyDeployedException;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
import org.openspaces.admin.space.ElasticSpaceDeployment;
import org.openspaces.admin.space.SpaceDeployment;

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
public interface GridServiceManagers extends AdminAware, Iterable<GridServiceManager>, DumpProvider {

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
     * Waits indefinitely till at least one GSM is discovered and returns it.
     */
    GridServiceManager waitForAtLeastOne();

    /**
     * Waits for the given timeout (in time unit) till at least one GSM is discovered and returns it.
     */
    GridServiceManager waitForAtLeastOne(long timeout, TimeUnit timeUnit);

    /**
     * Waits indefinitely till the provided number of managers are up. When passing 0, will wait
     * till there are no more managers.
     *
     * @param numberOfGridServiceManagers The number of managers to wait for
     */
    boolean waitFor(int numberOfGridServiceManagers);

    /**
     * Waits for the given timeout (in time unit) till the provided number of managers are up.
     * Returns <code>true</code> if the required number of managers were discovered, <code>false</code>
     * if the timeout expired.
     *
     * <p>When passing 0, will wait till there are no more managers.
     *
     * @param numberOfGridServiceManagers The number of managers to wait for
     */
    boolean waitFor(int numberOfGridServiceManagers, long timeout, TimeUnit timeUnit);

    /**
     * Deploys a processing unit based on the processing unit deployment information on a random grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     */
    ProcessingUnit deploy(ProcessingUnitDeployment deployment);

    /**
     * Deploys a processing unit based on the processing unit deployment information on a random grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     */
    ProcessingUnit deploy(ProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit);

    /**
     * Deploys a space based on the space deployment information on a random grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(SpaceDeployment deployment);

    /**
     * Deploys a memcached based on the space deployment information on a random grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the provided timeout and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(MemcachedDeployment deployment, long timeout, TimeUnit timeUnit);

    /**
     * Deploys a memcached based on the space deployment information on a random grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(MemcachedDeployment deployment);

    /**
     * Deploys a space based on the space deployment information on a random grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the provided timeout and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(SpaceDeployment deployment, long timeout, TimeUnit timeUnit);

    /**
     * Deploys an elastic space based on the space deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(ElasticSpaceDeployment deployment) throws ProcessingUnitAlreadyDeployedException;
    
    /**
     * Deploys an elastic space based on the space deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(ElasticSpaceDeployment deployment, long timeout, TimeUnit timeUnit) throws ProcessingUnitAlreadyDeployedException;

    /**
     * Deploys an elastic processing unit that has an embedded space based on the processing unit deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     * 
     * @throws ProcessingUnitAlreadyDeployedException - processing unit with the same name has already been deployed.
     */
    ProcessingUnit deploy(ElasticStatefulProcessingUnitDeployment deployment) throws ProcessingUnitAlreadyDeployedException;
    
    /**
     * Deploys an elastic processing unit that has an embedded space based on the processing unit deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     * 
     * @throws ProcessingUnitAlreadyDeployedException - processing unit with the same name has already been deployed.
     */
    ProcessingUnit deploy(ElasticStatefulProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit) throws ProcessingUnitAlreadyDeployedException;
    
    /**
     * Deploys an elastic processing unit that does not have an embedded space based on the processing unit deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     * 
     * @throws ProcessingUnitAlreadyDeployedException - processing unit with the same name has already been deployed.
     */
    ProcessingUnit deploy(ElasticStatelessProcessingUnitDeployment deployment) throws ProcessingUnitAlreadyDeployedException;
    
    /**
     * Deploys an elastic processing unit that does not have an embedded space based on the processing unit deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     * 
     * @throws ProcessingUnitAlreadyDeployedException - processing unit with the same name has already been deployed.
     */
    ProcessingUnit deploy(ElasticStatelessProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit) throws ProcessingUnitAlreadyDeployedException;

    /**
     * Deploys an application consisting of one or more processing unit deployments.
     *
     * <p>The deployment process will wait indefinitely
     *  
     * @return An application containing the actual processing units that can be used.
     * 
     * @throws ApplicationAlreadyDeployedException - Application with the same name has already been deployed.
     * @throws ProcessingUnitAlreadyDeployedException - Processing unit with the same name has already been deployed. Processing Unit names are globally unique (regardless of the application name)
     */
    Application deploy(ApplicationDeployment deployment) throws ApplicationAlreadyDeployedException, ProcessingUnitAlreadyDeployedException;
    
    /**
     * Deploys an application consisting of one or more processing unit deployments.
     *
     * <p>The deployment process will wait for the given timeout 
     * 
     * @return An application containing the actual processing units that can be used or null if timeout expired.
     * 
     * @throws ApplicationAlreadyDeployedException - Application with the same name has already been deployed.
     * @throws ProcessingUnitAlreadyDeployedException - Processing unit with the same name has already been deployed. Processing Unit names are globally unique (regardless of the application name)
     */
    Application deploy(ApplicationDeployment deployment, long timeout, TimeUnit timeUnit) throws ApplicationAlreadyDeployedException, ProcessingUnitAlreadyDeployedException;

    /**
     * Deploys an application consisting of one or more processing unit deployments.
     *
     * <p>The deployment process will wait indefinitely
     * 
     * @return An application containing the actual processing units that can be used.
     * 
     * @throws ApplicationAlreadyDeployedException - Application with the same name has already been deployed.
     * @throws ProcessingUnitAlreadyDeployedException - Processing unit with the same name has already been deployed. Processing Unit names are globally unique (regardless of the application name)
     */
    Application deploy(ApplicationConfig applicationConfig);
    
    /**
     * Deploys an application consisting of one or more processing unit deployments.
     *
     * <p>The deployment process will wait for the given timeout 
     * 
     * @return An application configuration containing the actual processing units that can be used or null if timeout expired.
     * 
     * @throws ApplicationAlreadyDeployedException - Application with the same name has already been deployed.
     * @throws ProcessingUnitAlreadyDeployedException - Processing unit with the same name has already been deployed. Processing Unit names are globally unique (regardless of the application name)
     */
    Application deploy(ApplicationConfig applicationConfig, long timeout, TimeUnit timeUnit)
            throws ApplicationAlreadyDeployedException, ProcessingUnitAlreadyDeployedException;   
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
