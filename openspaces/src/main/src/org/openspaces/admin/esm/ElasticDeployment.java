package org.openspaces.admin.esm;

import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.space.SpaceDeployment;

public class ElasticDeployment {
    ProcessingUnitDeployment deployment;
    
    public ElasticDeployment(ProcessingUnitDeployment deployment) {
        this.deployment = deployment;
    }
    
    public ElasticDeployment(SpaceDeployment deployment) {
        this.deployment = deployment.toProcessingUnitDeployment();
    }
}
