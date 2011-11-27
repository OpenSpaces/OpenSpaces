package org.openspaces.admin.internal.pu.dependency;

import org.jini.rio.core.RequiredDependencies;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependencies;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.pu.container.support.CommandLineParser;

public interface InternalProcessingUnitDependencies<T extends ProcessingUnitDependency, IT extends InternalProcessingUnitDependency> extends ProcessingUnitDependencies<T> {

    @Override
    InternalProcessingUnitDeploymentDependencies<T,IT> getDeploymentDependencies();
    
    void addDetailedDependencies(ProcessingUnitDetailedDependencies<? extends ProcessingUnitDependency> dependencies);

    void addDetailedDependenciesByCommandLineOption(String commandLineOption, RequiredDependencies requiredDependencies);

    CommandLineParser.Parameter[] toCommandLineParameters();
}
