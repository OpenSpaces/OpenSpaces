package org.openspaces.admin.application;

import java.util.ArrayList;
import java.util.List;

import org.openspaces.admin.internal.application.DefaultApplicationDeploymentOptions;
import org.openspaces.admin.internal.application.InternalApplicationDeploymentOptions;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;

public class ApplicationDeployment {

    private final String applicationName;
    private final List<ProcessingUnitDeploymentTopology> processingUnitDeployments;
    
    public ApplicationDeployment(String applicationName) {
        this.applicationName = applicationName;
        processingUnitDeployments = new ArrayList<ProcessingUnitDeploymentTopology>();
    }
    
    public ApplicationDeployment(String applicationName, ProcessingUnitDeploymentTopology ... processingUnitDeployments) {
        this(applicationName);
        for (ProcessingUnitDeploymentTopology puDeployment : processingUnitDeployments) {
            deployProcessingUnit(puDeployment);
        }
    }

    public ApplicationDeployment deployProcessingUnit(ProcessingUnitDeploymentTopology puDeployment) {
        processingUnitDeployments.add(puDeployment);
        return this;
    }
    
    public InternalApplicationDeploymentOptions getDeploymentOptions() {
        DefaultApplicationDeploymentOptions deploymentOptions = new DefaultApplicationDeploymentOptions(applicationName,processingUnitDeployments);
        return deploymentOptions;
    }

}
