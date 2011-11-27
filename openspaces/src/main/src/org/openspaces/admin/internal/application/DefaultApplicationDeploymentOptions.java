package org.openspaces.admin.internal.application;

import java.util.List;

import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;

public class DefaultApplicationDeploymentOptions implements InternalApplicationDeploymentOptions {

    private final String applicationName;
    private final List<ProcessingUnitDeploymentTopology> processingUnitDeployments;

    public DefaultApplicationDeploymentOptions(
            String applicationName,
            List<ProcessingUnitDeploymentTopology> processingUnitDeployments) {

        this.applicationName = applicationName;
        this.processingUnitDeployments = processingUnitDeployments;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public ProcessingUnitDeployment[] getProcessingUnitDeployments(Admin admin) {
        ProcessingUnitDeployment[] puDeployments = new ProcessingUnitDeployment[processingUnitDeployments.size()];
        for (int i = 0 ; i < puDeployments.length ; i++) {
            puDeployments[i] = processingUnitDeployments.get(i).toProcessingUnitDeployment(admin);
        }
        return puDeployments;
    }

    
}
