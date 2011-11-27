package org.openspaces.admin.internal.pu.dependency;

import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;

public class DefaultProcessingUnitDependencies
    extends AbstractProcessingUnitDependencies<ProcessingUnitDependency, InternalProcessingUnitDependency> 
    implements InternalProcessingUnitDependencies<ProcessingUnitDependency, InternalProcessingUnitDependency> {

    @Override
    public InternalProcessingUnitDeploymentDependencies<ProcessingUnitDependency, InternalProcessingUnitDependency> getDeploymentDependencies() {
        return super.getDetailedDependencies(new DefaultProcessingUnitDeploymentDependencies());
    }
}
