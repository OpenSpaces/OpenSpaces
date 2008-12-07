package org.openspaces.admin.pu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author kimchy
 */
public class ProcessingUnitDeployment {

    private final String processingUnit;

    private String name;

    private String clusterSchema;

    private Integer numberOfInstances;

    private Integer numberOfBackups;

    private Integer maxInstancesPerVM;

    private Integer maxInstancesPerMachine;

    private Properties contextProperties = new Properties();

    public ProcessingUnitDeployment(String processingUnit) {
        this.processingUnit = processingUnit;
    }

    public String getProcessingUnit() {
        return processingUnit;
    }

    public ProcessingUnitDeployment name(String name) {
        this.name = name;
        return this;
    }

    public ProcessingUnitDeployment clusterSchema(String clusterSchema) {
        this.clusterSchema = clusterSchema;
        return this;
    }

    public ProcessingUnitDeployment numberOfInstances(Integer numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
        return this;
    }

    public ProcessingUnitDeployment numberOfBackups(Integer numberOfBackups) {
        this.numberOfBackups = numberOfBackups;
        return this;
    }

    public ProcessingUnitDeployment maxInstancesPerVM(Integer maxInstancesPerVM) {
        this.maxInstancesPerVM = maxInstancesPerVM;
        return this;
    }

    public ProcessingUnitDeployment maxInstancesPerMachine(Integer maxInstancesPerMachine) {
        this.maxInstancesPerMachine = maxInstancesPerMachine;
        return this;
    }

    public ProcessingUnitDeployment setContextProperty(String key, String value) {
        contextProperties.put(key, value);
        return this;
    }

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
        for (Map.Entry entry : contextProperties.entrySet()) {
            deployOptions.add("-properties");
            deployOptions.add("embed://" + entry.getKey() + "=" + entry.getValue());
        }

        deployOptions.add(getProcessingUnit());

        return deployOptions.toArray(new String[deployOptions.size()]);
    }
}
