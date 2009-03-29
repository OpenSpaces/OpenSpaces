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

package org.openspaces.pu.sla;

import org.openspaces.pu.sla.monitor.Monitor;
import org.openspaces.pu.sla.requirement.Requirement;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.gigaspaces.grid.zone.ZoneHelper;

/**
 * A set of definitions controlling the nature of a processing unit deployment
 * on the Service Grid.
 *
 * <p>The SLA allows to control a clustered deployment which maps to
 * {@link org.openspaces.core.cluster.ClusterInfo ClusterInfo}. The
 * {@link #setClusterSchema(String) clusterSchema}, {@link #setNumberOfInstances(int) numberOfInstances},
 * and {@link #setNumberOfBackups(int) numberOfBackups} control the cluster
 * topology. Note that it is up to the user to defined the correct values
 * according to the chosen cluster schema. For example, when using <code>partitioned</code>
 * schema, the number of backups should not be set (or set to 0).
 *
 * <p>The SLA is translated into one or more service bean definitions, each can have
 * one or more instances. When having one or more backups, a service bean definition
 * will be created for each primary and its backups group. So, in case of 2 instances
 * with 1 backup, two service bean definition will be created, one for the first instance
 * and its backup, and the second for the second instance and its backup.
 *
 * <p>Max instances per VM can also be set which using {@link #setMaxInstancesPerVM(int)}.
 * It controls how many instances of a specific deployment will be created within a
 * grid container. This is very beneficial when using primary with backup where we would
 * not want the primary to run in the same VM as the backup.
 *
 * <p>A list of {@link org.openspaces.pu.sla.monitor.Monitor monitor}s
 * can be added to the SLA. The monitors can be used to monitor different <b>runtime</b> aspects
 * of the processing unit. They can then be used to affect certain {@link #setPolicy(Policy) Policy}
 * that is set.
 *
 * <p>A list of {@link org.openspaces.pu.sla.requirement.Requirement Requirement}s
 * can also be added to the SLA. The requirements control if a certain processing unit instance will be
 * <b>deployed</b> to a grid container. It will only be deployed if the grid container meets the specified
 * requirements.
 *
 * <p>A {@link org.openspaces.pu.sla.Policy Policy} can be set on the SLA to control
 * the actions the grid container should take in case the policy associated monitor value breach the
 * policy thresholds.
 *
 * @author kimchy
 */
public class SLA implements Serializable {

    private int numberOfInstances = 1;

    private int numberOfBackups = 0;

    private String clusterSchema;

    private Policy policy;

    private List<Requirement> requirements;

    private List<Monitor> monitors;

    private int maxInstancesPerVM;

    private int maxInstancesPerMachine;

    private Map<String, Integer> maxInstancesPerZone;

    private List<InstanceSLA> instanceSLAs;

    private MemberAliveIndicator memberAliveIndicator = new MemberAliveIndicator();

    /**
     * Returns the cluster schema the processing unit will use. Usually maps to the space
     * cluster schema. Can have <code>null</code> value which means that it was not set.
     *
     * @see org.openspaces.core.cluster.ClusterInfo#getSchema()
     */
    public String getClusterSchema() {
        return clusterSchema;
    }

    /**
     * Sets the cluster schema the processing unit will use. Usually maps to the space
     * cluster schema. Can have <code>null</code> value which means that it was not set.
     *
     * @see org.openspaces.core.cluster.ClusterInfo#setSchema(String)
     */
    public void setClusterSchema(String clusterSchema) {
        this.clusterSchema = clusterSchema;
    }

    /**
     * Returns the number of primary instances that are running within the cluster. Note, this are
     * the number of primary instances. Each instance might also have one or more backups that are
     * set by {@link #setNumberOfBackups(int) numberOfBackups}. Defaults to <code>1</code>.
     *
     * @see org.openspaces.core.cluster.ClusterInfo#getNumberOfInstances()
     */
    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    /**
     * Sets the number of primary instances that are running within the cluster. Note, this are the
     * number of primary instances. Each instance might also have one or more backups that are set
     * by {@link #setNumberOfBackups(int) numberOfBackups}. . Defaults to <code>1</code>.
     */
    public void setNumberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    /**
     * Returns the number of backups that each primary instance will have in a cluster. Can have
     * <code>0</code> value which means that it was not set and should not be taken into
     * account. Defaults to <code>0</code>.
     */
    public int getNumberOfBackups() {
        return numberOfBackups;
    }

    /**
     * Sets the number of backups that each primary instance will have in a cluster. Can have
     * <code>0</code> value which means that it was not set and should not be taken into
     * account. Defaults to <code>0</code>.
     */
    public void setNumberOfBackups(int numberOfBackups) {
        this.numberOfBackups = numberOfBackups;
    }

    /**
     * Returns the maximum number of instances of the processing unit allowed to run within
     * the same grid container.
     *
     * <p>Note, when using number of backups higher than 0, this value only applies to a
     * primary with its backups group.
     */
    public int getMaxInstancesPerVM() {
        return maxInstancesPerVM;
    }

    /**
     * Sets the maximum number of instances of the processing unit allowed to run within
     * the same grid container.
     *
     * <p>Note, when using number of backups higher than 0, this value only applies to a
     * primary with its backups group.
     */
    public void setMaxInstancesPerVM(int maxInstancesPerVM) {
        this.maxInstancesPerVM = maxInstancesPerVM;
    }

    /**
     * Returns the maximum number of instances of the processing unit allowed to run
     * on the same machine, regardless of the number of grid containers running on it.
     *
     * <p>Note, when using number of backups higher than 0, this value only applies to a
     * primary with its backups group.
     */
    public int getMaxInstancesPerMachine() {
        return maxInstancesPerMachine;
    }

    /**
     * Sets the maximum number of instances of the processing unit allowed to run
     * on the same machine, regardless of the number of grid containers running on it.
     *
     * <p>Note, when using number of backups higher than 0, this value only applies to a
     * primary with its backups group.
     */
    public void setMaxInstancesPerMachine(int maxInstancesPerMachine) {
        this.maxInstancesPerMachine = maxInstancesPerMachine;
    }

    public Map<String, Integer> getMaxInstancesPerZone() {
        return maxInstancesPerZone;
    }

    public void setMaxInstancesPerZone(Map<String, Integer> maxInstancesPerZone) {
        this.maxInstancesPerZone = maxInstancesPerZone;
    }

    public void setMaxInstancesPerZoneAsString(String maxInstancesPerZone) {
        this.maxInstancesPerZone = ZoneHelper.parse(maxInstancesPerZone);
    }

    /**
     * Returns the policy associated with the SLA controlling the runtime policy on the
     * action needed to be taken when the monitor associated with the policy breaks the
     * policy thresholds.
     */
    public Policy getPolicy() {
        return policy;
    }

    /**
     * Sets the policy associated with the SLA controlling the runtime policy on the
     * action needed to be taken when the monitor associated with the policy breaks the
     * policy thresholds.
     */
    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    /**
     * Returns a list of requirements that control if a certain processing unit instance will be
     * <b>deployed</b> to a grid container. It will only be deployed if the grid container meets the specified
     * requirements.
     */
    public List<Requirement> getRequirements() {
        return requirements;
    }

    /**
     * Sets a list of requirements that control if a certain processing unit instance will be
     * <b>deployed</b> to a grid container. It will only be deployed if the grid container meets the specified
     * requirements.
     */
    public void setRequirements(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    /**
     * Returns a list of monitors can be used to monitor different <b>runtime</b> aspects
     * of the processing unit. They can then be used to affect certain {@link #setPolicy(Policy) Policy}
     * that is set.
     */
    public List<Monitor> getMonitors() {
        return monitors;
    }

    /**
     * Sets a list of monitors can be used to monitor different <b>runtime</b> aspects
     * of the processing unit. They can then be used to affect certain {@link #setPolicy(Policy) Policy}
     * that is set.
     */
    public void setMonitors(List<Monitor> monitors) {
        this.monitors = monitors;
    }

    public List<InstanceSLA> getInstanceSLAs() {
        return instanceSLAs;
    }

    public void setInstanceSLAs(List<InstanceSLA> instanceSLAs) {
        this.instanceSLAs = instanceSLAs;
    }

    public MemberAliveIndicator getMemberAliveIndicator() {
        return memberAliveIndicator;
    }

    public void setMemberAliveIndicator(MemberAliveIndicator memberAliveIndicator) {
        this.memberAliveIndicator = memberAliveIndicator;
    }

    public String toString() {
        return "numberOfInstances [" + numberOfInstances + "] numberOfBackups [" + numberOfBackups
                + "] clusterSchema [" + clusterSchema + "] policy " + policy;
    }

}
