package org.openspaces.admin.internal.pu.dependency;

import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.dependency.ProcessingUnitDeploymentDependencies;

public interface InternalProcessingUnitDeploymentDependencies<T extends ProcessingUnitDependency, IT extends InternalProcessingUnitDependency> 
    
        extends InternalProcessingUnitDetailedDependencies<T,IT>, 
                ProcessingUnitDeploymentDependencies<T> {

}
