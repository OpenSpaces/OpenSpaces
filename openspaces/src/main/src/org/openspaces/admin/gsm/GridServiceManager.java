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

import org.openspaces.admin.AgentGridComponent;
import org.openspaces.admin.LogProviderGridComponent;
import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.memcached.MemcachedDeployment;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.space.SpaceDeployment;

import java.util.concurrent.TimeUnit;

/**
 * A Grid Service Manager is a manager for {@link org.openspaces.admin.pu.ProcessingUnit} deployments
 * (acting either as primary or backups for a certain processing unit deployment). It also knows
 * which {@link org.openspaces.admin.gsc.GridServiceContainer}s are around to be able to create
 * {@link org.openspaces.admin.pu.ProcessingUnitInstance} on them.
 *
 * @author kimchy
 */
public interface GridServiceManager extends AgentGridComponent, LogProviderGridComponent, DumpProvider {

    /**
     * Deploys a processing unit based on the processing unit deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     */
    ProcessingUnit deploy(ProcessingUnitDeployment deployment);

    /**
     * Deploys a processing unit based on the processing unit deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     */
    ProcessingUnit deploy(ProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit);

    /**
     * Deploys a space based on the space deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(SpaceDeployment deployment);

    /**
     * Deploys a space based on the space deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(SpaceDeployment deployment, long timeout, TimeUnit timeUnit);

    /**
     * Deploys a memcached based on the space deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a memcached is simply deploying a built in processing unit that starts / connects
     * to a space (holding the memcached entries) and exposing the memcached protocol.
     */
    ProcessingUnit deploy(MemcachedDeployment deployment);

    /**
     * Deploys a memcached based on the space deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a memcached is simply deploying a built in processing unit that starts / connects
     * to a space (holding the memcached entries) and exposing the memcached protocol.
     */
    ProcessingUnit deploy(MemcachedDeployment deployment, long timeout, TimeUnit timeUnit);
}
