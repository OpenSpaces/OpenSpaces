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

package org.openspaces.admin.pu;

import com.gigaspaces.grid.zone.ZoneHelper;

import java.io.File;
import java.util.*;

/**
 * A deployment of processing unit.
 *
 * @author kimchy
 * @see org.openspaces.admin.gsm.GridServiceManager#deploy(ProcessingUnitDeployment)
 * @see org.openspaces.admin.gsm.GridServiceManagers#deploy(ProcessingUnitDeployment)
 */
public class ProcessingUnitDeployment {

    private final String processingUnit;

    private String name;

    private String clusterSchema;

    private Integer numberOfInstances;

    private Integer numberOfBackups;

    private Integer maxInstancesPerVM;

    private Integer maxInstancesPerMachine;

    private final Map<String, Integer> maxInstancesPerZone = new HashMap<String, Integer>();

    private final List<String> zones = new ArrayList<String>();

    private final Properties contextProperties = new Properties();

    /**
     * Constructs a processing unit deployment based on the specified processing unit name (should
     * exists under the <code>[GS ROOT]/deploy</code> directory.
     */
    public ProcessingUnitDeployment(String processingUnit) {
        this.processingUnit = processingUnit;
    }

    /**
     * Constructs a processing unit deployment based on the specified processing unit file path (points either
     * to a processing unit jar file or a directory).
     */
    public ProcessingUnitDeployment(File processingUnit) {
        this.processingUnit = processingUnit.getAbsolutePath();
    }

    /**
     * Returns the processing unit that will be deployed.
     */
    public String getProcessingUnit() {
        return processingUnit;
    }

    /**
     * Sets the processing unit name that will be deployed. By default it will be based on the
     * parameter passed in the constructor.
     */
    public ProcessingUnitDeployment name(String name) {
        this.name = name;
        return this;
    }

    /**
     * A convenient method allowing to easily configure the space deployment to deploy a
     * partitioned topology with <code>numberOfParitions</code> instances each with
     * <code>numberOfBackups</code>.
     *
     * <p>Exactly the same like calling <code>clusterSchema("partitioned-sync2backup")</code>,
     * followed by <code>numberOfInstances(numberOfParitions)</code> and <code>numberOfBackups(numberOfBackups)</code>.
     *
     * @param numberOfParitions The number of partitions
     * @param numberOfBackups   The number of backups
     * @return This space deployment
     */
    public ProcessingUnitDeployment partitioned(int numberOfParitions, int numberOfBackups) {
        clusterSchema("partitioned-sync2backup");
        numberOfInstances(numberOfParitions);
        numberOfBackups(numberOfBackups);
        return this;
    }

    /**
     * A convenient method allowing to easily configure the space deployment to deploy a
     * replicated topology with <code>numberOfInstances</code> instances.
     *
     * <p>Exactly the same like calling <code>clusterSchema("replicated")</code>,
     * followed by <code>numberOfInstances(numberOfInstances)</code> and <code>numberOfBackups(0)</code>.
     *
     * @param numberOfInstances The number of instances to form the replicated space
     * @return This space deployment
     */
    public ProcessingUnitDeployment replicated(int numberOfInstances) {
        clusterSchema("replicated");
        numberOfInstances(numberOfInstances);
        numberOfBackups(0);
        return this;
    }

    /**
     * Controls the cluster schema of the deployment. Only make sense to set it when there is an embedded space
     * defined within the processing unit.
     */
    public ProcessingUnitDeployment clusterSchema(String clusterSchema) {
        this.clusterSchema = clusterSchema;
        return this;
    }

    /**
     * Sets the number of instances that will be deployed as part of this processing unit instance.
     */
    public ProcessingUnitDeployment numberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
        return this;
    }

    /**
     * Sets the number of backups that will be deployed as part of this processing unit. Only applicable
     * when the processing unit has an embedded space.
     */
    public ProcessingUnitDeployment numberOfBackups(int numberOfBackups) {
        this.numberOfBackups = numberOfBackups;
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
    public ProcessingUnitDeployment maxInstancesPerVM(int maxInstancesPerVM) {
        this.maxInstancesPerVM = maxInstancesPerVM;
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
    public ProcessingUnitDeployment maxInstancesPerMachine(int maxInstancesPerMachine) {
        this.maxInstancesPerMachine = maxInstancesPerMachine;
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
    public ProcessingUnitDeployment maxInstancesPerZone(String zone, int maxInstancesPerZone) {
        this.maxInstancesPerZone.put(zone, maxInstancesPerZone);
        return this;
    }

    /**
     * Adds a zone where the processing unit is allowed to be deployed on.
     */
    public ProcessingUnitDeployment addZone(String zone) {
        zones.add(zone);
        return this;
    }

    /**
     * Sets a context deploy time property overriding any <code>${...}</code> defined within a processing
     * unit configuration.
     */
    public ProcessingUnitDeployment setContextProperty(String key, String value) {
        contextProperties.put(key, value);
        return this;
    }

    /**
     * Transforms this deployment into a set of deployment options.
     */
    public String[] getDeploymentOptions() {
        List<String> deployOptions = new ArrayList<String>();

        if (name != null) {
            deployOptions.add("-override-name");
            deployOptions.add(name);
        }
        if (clusterSchema != null || numberOfInstances != null || numberOfBackups != null) {
            deployOptions.add("-cluster");
            if (clusterSchema != null) {
                deployOptions.add("schema=" + clusterSchema);
            }
            if (numberOfInstances != null) {
                String totalMembers = "total_members=" + numberOfInstances;
                if (numberOfBackups != null) {
                    totalMembers += "," + numberOfBackups;
                }
                deployOptions.add(totalMembers);
            }
        }
        if (maxInstancesPerVM != null) {
            deployOptions.add("-max-instances-per-vm");
            deployOptions.add(maxInstancesPerVM.toString());
        }
        if (maxInstancesPerMachine != null) {
            deployOptions.add("-max-instances-per-machine");
            deployOptions.add(maxInstancesPerMachine.toString());
        }
        if (!maxInstancesPerZone.isEmpty()) {
            deployOptions.add("-max-instances-per-zone");
            deployOptions.add(ZoneHelper.unparse(maxInstancesPerZone));
        }
        if (!zones.isEmpty()) {
            deployOptions.add("-zones");
            for (String requiredZone : zones) {
                deployOptions.add(requiredZone);
            }
        }
        for (Map.Entry entry : contextProperties.entrySet()) {
            deployOptions.add("-properties");
            deployOptions.add("embed://" + entry.getKey() + "=" + entry.getValue());
        }

        deployOptions.add(getProcessingUnit());

        return deployOptions.toArray(new String[deployOptions.size()]);
    }
}
