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

package org.openspaces.admin.space;

import java.io.File;

import org.openspaces.admin.internal.pu.dependency.ProcessingUnitDetailedDependencies;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.dependency.ProcessingUnitDeploymentDependenciesConfigurer;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigFactory;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;
import org.openspaces.admin.space.config.SpaceConfig;

import com.gigaspaces.security.directory.User;
import com.gigaspaces.security.directory.UserDetails;

/**
 * A deployment of a pure {@link org.openspaces.admin.space.Space} processing unit (comes built in
 * under <code>[GS ROOT]/deploy/templates/datagrid</code>).
 *
 * @author kimchy
 * @see org.openspaces.admin.gsm.GridServiceManager#deploy(SpaceDeployment)
 * @see org.openspaces.admin.gsm.GridServiceManagers#deploy(SpaceDeployment)
 */
public class SpaceDeployment implements ProcessingUnitDeploymentTopology {

    private final SpaceConfig config;
    
    /**
     * Constructs a new Space deployment with the space name that will be created (it will also
     * be the processing unit name).
     */
    public SpaceDeployment(String spaceName) {
        config = new SpaceConfig();
        config.setName(spaceName);
    }

    /**
     * Returns the Space name of the config.set
     * Use {@link SpaceConfig#getName()} instead
     */
    @Deprecated
    public String getSpaceName() {
        return config.getName();
    }

    /**
     * A convenient method allowing to easily configure the space deployment to deploy a
     * Partitioned topology with <code>numberOfParitions</code> instances each with
     * <code>numberOfBackups</code>.
     *
     * <p>Exactly the same like calling <code>clusterSchema("partitioned-sync2backup")</code>,
     * followed by <code>numberOfInstances(numberOfParitions)</code> and <code>numberOfBackups(numberOfBackups)</code>.
     *
     * @param numberOfPartitions The number of partitions
     * @param numberOfBackups   The number of backups
     * @return This space deployment
     */
    public SpaceDeployment partitioned(int numberOfPartitions, int numberOfBackups) {
        clusterSchema("partitioned-sync2backup");
        numberOfInstances(numberOfPartitions);
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
    public SpaceDeployment replicated(boolean async, int numberOfInstances) {
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
    public SpaceDeployment clusterSchema(String clusterSchema) {
        config.setClusterSchema(clusterSchema);
        return this;
    }

    /**
     * Sets the number of instances of the space config.set
     */
    public SpaceDeployment numberOfInstances(int numberOfInstances) {
        config.setNumberOfInstances(numberOfInstances);
        return this;
    }

    /**
     * Sets the number of backups per instance of the space config.set
     */
    public SpaceDeployment numberOfBackups(int numberOfBackups) {
        config.setNumberOfBackups(numberOfBackups);
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
    public SpaceDeployment maxInstancesPerVM(int maxInstancesPerVM) {
        config.setMaxInstancesPerVM(maxInstancesPerVM);
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
    public SpaceDeployment maxInstancesPerMachine(int maxInstancesPerMachine) {
        config.setMaxInstancesPerMachine(maxInstancesPerMachine);
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
    public SpaceDeployment maxInstancesPerZone(String zone, int maxInstancesPerZone) {
        config.getMaxInstancesPerZone().put(zone, maxInstancesPerZone);
        return this;
    }

    /**
     * Adds a zone where the processing unit is allowed to be deployed on.
     */
    public SpaceDeployment addZone(String zone) {
        config.getZones().add(zone);
        return this;
    }

    /**
     * Sets a context deploy time property overriding any <code>${...}</code> defined within a processing
     * unit configuration.
     */
    public SpaceDeployment setContextProperty(String key, String value) {
        config.setContextProperty(key, value);
        return this;
    }

    /**
     * Will deploy a secured space. Note, by setting user details the space will be secured automatically.
     */
    public SpaceDeployment secured(boolean secured) {
        config.setSecured(secured);
        return this;
    }

    /**
     * Advance: Sets the security user details for authentication and autherization of the
     * processing unit.
     */
    public SpaceDeployment userDetails(UserDetails userDetails) {
        config.setUserDetails(userDetails);
        return this;
    }

    /**
     * Sets the username and password (effectively making the processing unit secured)
     * for the processing unit config.set
     */
    public SpaceDeployment userDetails(String userName, String password) {
        config.setUserDetails(new User(userName,password));
        return this;
    }

    /**
     * Sets an external SLA definition location to be loaded.
     */
    public SpaceDeployment slaLocation(String slaLocation) {
        config.setSlaLocation(slaLocation);
        return this;
    }

    /**
     * Sets an external SLA definition location to be loaded.
     */
    public SpaceDeployment slaLocation(File slaLocation) {
        slaLocation(slaLocation.getAbsolutePath());
        return this;
    }

    /**
     * Postpones deployment of processing unit instances until the specified dependencies are met.
     * 
     * The following example postpones the deployment of this processing unit until B has completed the deployment and C has at least one instance deployed.
     * config.setaddDependencies(new ProcessingUnitDeploymentDependenciesConfigurer().dependsOnDeployment("B").dependsOnMinimumNumberOfDeployedInstances("C",1).create())
     * 
     * @see ProcessingUnitDeploymentDependenciesConfigurer
     * @since 8.0.6
     */
    @Override
    public SpaceDeployment addDependencies(
            ProcessingUnitDetailedDependencies<? extends ProcessingUnitDependency> deploymentDependencies) {
        config.getDependencies().addDetailedDependencies(deploymentDependencies);
        return this;
    }
    /**
     * Postpones deployment of processing unit instances deployment until the specified processing unit deployment is complete.
     * 
     * Same as: config.setaddDependencies(new ProcessingUnitDeploymentDependenciesConfigurer().dependsOnDeployment(requiredProcessingUnitName).create())
     * 
     * @since 8.0.6
     */
    @Override
    public SpaceDeployment addDependency(String requiredProcessingUnitName) {
        addDependencies(
                new ProcessingUnitDeploymentDependenciesConfigurer()
                .dependsOnDeployed(requiredProcessingUnitName)
                .create());
        return this;
    }

    @Override
    public ProcessingUnitConfigFactory create() {
        return config;
    }
}
