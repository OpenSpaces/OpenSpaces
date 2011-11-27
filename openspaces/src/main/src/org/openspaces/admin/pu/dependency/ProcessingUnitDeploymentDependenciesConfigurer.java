package org.openspaces.admin.pu.dependency;

import org.openspaces.admin.internal.pu.dependency.AbstractProcessingUnitDependenciesConfigurer;
import org.openspaces.admin.internal.pu.dependency.DefaultProcessingUnitDependencyFactory;
import org.openspaces.admin.internal.pu.dependency.DefaultProcessingUnitDeploymentDependencies;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependency;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDeploymentDependencies;

/**
 * A configurer (builder) object used to create a new {@link ProcessingUnitDeploymentDependencies}
 * 
 * @author itaif
 * @see ProcessingUnitDependency
 */
public class ProcessingUnitDeploymentDependenciesConfigurer 
    extends AbstractProcessingUnitDependenciesConfigurer<
        ProcessingUnitDependency,
        InternalProcessingUnitDependency,
        ProcessingUnitDeploymentDependencies<ProcessingUnitDependency>,
        InternalProcessingUnitDeploymentDependencies<ProcessingUnitDependency, InternalProcessingUnitDependency>>{
    
    public ProcessingUnitDeploymentDependenciesConfigurer() {
        super(new DefaultProcessingUnitDependencyFactory(), new DefaultProcessingUnitDeploymentDependencies());
    }

}
