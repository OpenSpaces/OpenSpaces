package org.openspaces.admin.internal.pu.dependency;

import org.jini.rio.core.RequiredDependencies;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.dependency.ProcessingUnitDeploymentDependencies;

public class DefaultProcessingUnitDeploymentDependencies 
    extends AbstractProcessingUnitDetailedDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> 
    implements InternalProcessingUnitDeploymentDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> {

    public DefaultProcessingUnitDeploymentDependencies() {
        super(new DefaultProcessingUnitDependencyFactory());
    }

    public DefaultProcessingUnitDeploymentDependencies(RequiredDependencies requiredDependencies) {
        this();
        addDependencies(requiredDependencies);
    }
    
    @Override
    public String getCommandLineOption() {
        return ProcessingUnitDeploymentDependencies.COMMANDLINE_OPTION;
    }

}
