package org.openspaces.admin.application;

import java.util.ArrayList;
import java.util.List;

import org.openspaces.admin.internal.application.DefaultApplicationDeploymentOptions;
import org.openspaces.admin.internal.application.InternalApplicationDeploymentOptions;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;

/**
 * Describes an application deployment that consists of one or more processing unit deployments.
 * @since 8.0.6
 * @author itaif
 */
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

    /**
     * Deprecated Method. Use {@link #addProcessingUnitDeployment(ProcessingUnitDeploymentTopology)} instead
     */
    @Deprecated
    public ApplicationDeployment deployProcessingUnit(ProcessingUnitDeploymentTopology puDeployment) {
        return addProcessingUnitDeployment(puDeployment);
    }

    /**
     * Adds a processing unit deployment to this application deployment.
     * All processing units are deployed in parallel (unless dependencies are defined)
     */
    public ApplicationDeployment addProcessingUnitDeployment(ProcessingUnitDeploymentTopology puDeployment) {
        processingUnitDeployments.add(puDeployment);
        return this;
    }
    
    public InternalApplicationDeploymentOptions getDeploymentOptions() {
        DefaultApplicationDeploymentOptions deploymentOptions = new DefaultApplicationDeploymentOptions(applicationName,processingUnitDeployments);
        return deploymentOptions;
    }

}
