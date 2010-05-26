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

package org.openspaces.admin.memcached;

import com.gigaspaces.security.directory.UserDetails;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.pu.container.servicegrid.deploy.MemcachedDeploy;

import java.io.File;

/**
 * A deployment of a pure memcached processing unit (comes built in
 * under <code>[GS ROOT]/deploy/templates/memcached</code>).
 *
 * @author kimchy
 * @see org.openspaces.admin.gsm.GridServiceManager#deploy(org.openspaces.admin.memcached.MemcachedDeployment)
 * @see org.openspaces.admin.gsm.GridServiceManagers#deploy(org.openspaces.admin.memcached.MemcachedDeployment)
 */
public class MemcachedDeployment {

    private final ProcessingUnitDeployment deployment;

    private final String spaceUrl;

    /**
     * Constructs a new Space deployment with the space name that will be created (it will also
     * be the processing unit name).
     */
    public MemcachedDeployment(String spaceUrl) {
        this.spaceUrl = spaceUrl;
        this.deployment = new ProcessingUnitDeployment("/templates/memcached");
        this.deployment.name(MemcachedDeploy.extractName(spaceUrl) + "-memcached");
        this.deployment.setContextProperty("url", spaceUrl);
    }

    /**
     * Returns the Space url of the deployment.
     */
    public String getSpaceUrl() {
        return spaceUrl;
    }

    /**
     * A convenient method allowing to easily configure the space deployment to deploy a
     * Partitioned topology with <code>numberOfParitions</code> instances each with
     * <code>numberOfBackups</code>.
     *
     * <p>Exactly the same like calling <code>clusterSchema("partitioned-sync2backup")</code>,
     * followed by <code>numberOfInstances(numberOfParitions)</code> and <code>numberOfBackups(numberOfBackups)</code>.
     *
     * @param numberOfParitions The number of partitions
     * @param numberOfBackups   The number of backups
     * @return This space deployment
     */
    public MemcachedDeployment partitioned(int numberOfParitions, int numberOfBackups) {
        clusterSchema("partitioned-sync2backup");
        numberOfInstances(numberOfParitions);
        numberOfBackups(numberOfBackups);
        return this;
    }

    /**
     * A convenient method allowing to easily configure the space deployment to deploy a
     * replicated (either sync or async) topology with <code>numberOfInstances</code> instances.
     *
     * <p>Exactly the same like calling <code>clusterSchema("sync_replicated")</code> or <code>clusterSchema("async_replicated")</code>,
     * followed by <code>numberOfInstances(numberOfInstances)</code> and <code>numberOfBackups(0)</code>.
     *
     * @param numberOfInstances The number of instances to form the replicated space
     * @return This space deployment
     */
    public MemcachedDeployment replicated(boolean async, int numberOfInstances) {
        if (async) {
            clusterSchema("async_replicated");
        } else {
            clusterSchema("sync_replicated");
        }
        numberOfInstances(numberOfInstances);
        numberOfBackups(0);
        return this;
    }

    /**
     * Sets the cluster schema of the Space.
     *
     * @see #partitioned(int, int)
     */
    public MemcachedDeployment clusterSchema(String clusterSchema) {
        deployment.clusterSchema(clusterSchema);
        return this;
    }

    /**
     * Sets the number of instances of the space deployment.
     */
    public MemcachedDeployment numberOfInstances(int numberOfInstances) {
        deployment.numberOfInstances(numberOfInstances);
        return this;
    }

    /**
     * Sets the number of backups per instance of the space deployment.
     */
    public MemcachedDeployment numberOfBackups(int numberOfBackups) {
        deployment.numberOfBackups(numberOfBackups);
        return this;
    }

    /**
     * Sets the maximum number of instances per virtual machine.
     *
     * <p>On partitioned topology with backups topology, controls that a primary and a backup won't run
     * on the same virtual machine if set to <code>1</code>.
     *
     * <p>On a non partitioned with backups topology, controls the maximum number of instances running on
     * the same virtual machine.
     */
    public MemcachedDeployment maxInstancesPerVM(int maxInstancesPerVM) {
        deployment.maxInstancesPerVM(maxInstancesPerVM);
        return this;
    }

    /**
     * Sets the maximum number of instances per machine.
     *
     * <p>On partitioned topology with backups topology, controls that a primary and a backup won't run
     * on the same machine if set to <code>1</code>.
     *
     * <p>On a non partitioned with backups topology, controls the maximum number of instances running on
     * the same machine.
     */
    public MemcachedDeployment maxInstancesPerMachine(int maxInstancesPerMachine) {
        deployment.maxInstancesPerMachine(maxInstancesPerMachine);
        return this;
    }

    /**
     * Sets the maximum number of instances per zone.
     *
     * <p>On partitioned topology with backups topology, controls that a primary and a backup won't run
     * on the same zone if set to <code>1</code>. Note, for each zone this will have to be set.
     *
     * <p>On a non partitioned with backups topology, controls the maximum number of instances running on
     * the same zone.
     */
    public MemcachedDeployment maxInstancesPerZone(String zone, int maxInstancesPerZone) {
        deployment.maxInstancesPerZone(zone, maxInstancesPerZone);
        return this;
    }

    /**
     * Adds a zone where the processing unit is allowed to be deployed on.
     */
    public MemcachedDeployment addZone(String zone) {
        deployment.addZone(zone);
        return this;
    }

    /**
     * Sets a context deploy time property overriding any <code>${...}</code> defined within a processing
     * unit configuration.
     */
    public MemcachedDeployment setContextProperty(String key, String value) {
        deployment.setContextProperty(key, value);
        return this;
    }

    /**
     * Will deploy a secured space. Note, by setting user details the space will be secured automatically.
     */
    public MemcachedDeployment secured(boolean secured) {
        deployment.secured(secured);
        return this;
    }

    /**
     * Advance: Sets the security user details for authentication and autherization of the
     * processing unit.
     */
    public MemcachedDeployment userDetails(UserDetails userDetails) {
        deployment.userDetails(userDetails);
        return this;
    }

    /**
     * Sets the username and password (effectively making the processing unit secured)
     * for the processing unit deployment.
     */
    public MemcachedDeployment userDetails(String userName, String password) {
        deployment.userDetails(userName, password);
        return this;
    }

    /**
     * Sets an external SLA definition location to be loaded.
     */
    public MemcachedDeployment slaLocation(String slaLocation) {
        deployment.slaLocation(slaLocation);
        return this;
    }

    /**
     * Sets an external SLA definition location to be loaded.
     */
    public MemcachedDeployment slaLocation(File slaLocation) {
        deployment.slaLocation(slaLocation);
        return this;
    }

    /**
     * Transforms the space deployment to a processing unit deployment (it is a processing unit after all,
     * that simply starts an embedded space).
     */
    public ProcessingUnitDeployment toProcessingUnitDeployment() {
        return deployment;
    }
}