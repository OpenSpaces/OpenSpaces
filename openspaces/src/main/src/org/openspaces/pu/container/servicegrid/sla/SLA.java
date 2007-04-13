package org.openspaces.pu.container.servicegrid.sla;

import java.io.Serializable;
import java.util.List;

/**
 */
public class SLA implements Serializable {

    private int numberOfInstances = 1;

    private int numberOfBackups = 0;

    private String clusterSchema;

    private Policy policy;

    private List<Requirement> requirements;

    private int maxInstancesPerVM;

    public String getClusterSchema() {
        return clusterSchema;
    }

    public void setClusterSchema(String clusterSchema) {
        this.clusterSchema = clusterSchema;
    }

    public int getMaxInstancesPerVM() {
        return maxInstancesPerVM;
    }

    public void setMaxInstancesPerVM(int maxInstancesPerVM) {
        this.maxInstancesPerVM = maxInstancesPerVM;
    }

    public int getNumberOfBackups() {
        return numberOfBackups;
    }

    public void setNumberOfBackups(int numberOfBackups) {
        this.numberOfBackups = numberOfBackups;
    }

    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    public String toString() {
        return "numberOfInstances [" + numberOfInstances + "] numberOfBackups [" + numberOfBackups
                + "] clusterSchema [" + clusterSchema + "] policy " + policy;
    }

}
