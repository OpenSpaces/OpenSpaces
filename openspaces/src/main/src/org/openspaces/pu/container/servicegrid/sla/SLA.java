package org.openspaces.pu.container.servicegrid.sla;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Feb 13, 2007
 * Time: 12:50:39 AM
 */
public class SLA {
// ------------------------------ FIELDS ------------------------------

    int numberOfInstances = 1;

    int numberOfBackups = 0;

    String clusterSchema;

    Policy policy;

    List requirements;

    int maxInstancesPerVM;

// --------------------- GETTER / SETTER METHODS ---------------------

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

    public List getRequirements() {
        return requirements;
    }

    public void setRequirements(List requirements) {
        this.requirements = requirements;
    }

// ------------------------ CANONICAL METHODS ------------------------

    public String toString() {
        return "SLA{" +
                "\n\tnumberOfInstances=" + numberOfInstances +
                "\n\tnumberOfBackups=" + numberOfBackups +
                "\n\tclusterSchema='" + clusterSchema + '\'' +
                "\n\tpolicy=" + policy +
                "\n}";
    }

// -------------------------- OTHER METHODS --------------------------

}
