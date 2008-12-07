package org.openspaces.admin.space;

import org.openspaces.admin.pu.ProcessingUnitDeployment;

/**
 * @author kimchy
 */
public class SpaceDeployment {

    private final ProcessingUnitDeployment deployment;

    private final String spaceName;

    public SpaceDeployment(String spaceName) {
        this.spaceName = spaceName;
        this.deployment = new ProcessingUnitDeployment("/templates/datagrid");
        this.deployment.name(spaceName);
        this.deployment.setContextProperty("dataGridName", spaceName);
    }

    public String getSpaceName() {
        return spaceName;
    }

    public SpaceDeployment clusterSchema(String clusterSchema) {
        deployment.clusterSchema(clusterSchema);
        return this;
    }

    public SpaceDeployment numberOfInstances(Integer numberOfInstances) {
        deployment.numberOfInstances(numberOfInstances);
        return this;
    }

    public SpaceDeployment numberOfBackups(Integer numberOfBackups) {
        deployment.numberOfBackups(numberOfBackups);
        return this;
    }

    public SpaceDeployment maxInstancesPerVM(Integer maxInstancesPerVM) {
        deployment.maxInstancesPerVM(maxInstancesPerVM);
        return this;
    }

    public SpaceDeployment maxInstancesPerMachine(Integer maxInstancesPerMachine) {
        deployment.maxInstancesPerMachine(maxInstancesPerMachine);
        return this;
    }

    public SpaceDeployment setContextProperty(String key, String value) {
        deployment.setContextProperty(key, value);
        return this;
    }

    public ProcessingUnitDeployment toProcessingUnitDeployment() {
        return deployment;
    }
}
