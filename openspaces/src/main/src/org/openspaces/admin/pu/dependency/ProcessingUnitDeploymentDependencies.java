package org.openspaces.admin.pu.dependency;

import org.openspaces.admin.internal.pu.dependency.ProcessingUnitDetailedDependencies;
import org.openspaces.pu.container.support.RequiredDependenciesCommandLineParser;

/**
 * Defines a dependency that postpones the processing unit instance deployment.
 * The instance is deployed only after the dependencies described by this interface are met.
 * 
 * @since 8.0.6
 * @author itaif
 *
 */
public interface ProcessingUnitDeploymentDependencies<T extends ProcessingUnitDependency> 
    extends ProcessingUnitDetailedDependencies<T> {

    public final String COMMANDLINE_OPTION = RequiredDependenciesCommandLineParser.INSTANCE_DEPLOYMENT_REQUIRED_DEPENDENCIES_PARAMETER_NAME;

}
