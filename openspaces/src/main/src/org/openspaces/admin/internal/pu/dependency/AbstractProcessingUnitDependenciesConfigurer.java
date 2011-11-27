package org.openspaces.admin.internal.pu.dependency;

import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;

public abstract class AbstractProcessingUnitDependenciesConfigurer<
    T extends ProcessingUnitDependency, 
    IT extends InternalProcessingUnitDependency,
    S extends ProcessingUnitDetailedDependencies<T>,
    IS extends InternalProcessingUnitDetailedDependencies<T,IT>> {

    private final IS dependencies;
    private final InternalProcessingUnitDependencyFactory<IT> dependencyFactory;
    
    public AbstractProcessingUnitDependenciesConfigurer(InternalProcessingUnitDependencyFactory<IT> dependencyFactory, IS dependencies) {
        this.dependencyFactory = dependencyFactory;
        this.dependencies = dependencies;
    }
    
    @SuppressWarnings("unchecked") // IS extends S
    public S create() {
        return (S)dependencies;
    }

    public AbstractProcessingUnitDependenciesConfigurer<T,IT,S,IS> dependsOnMinimumNumberOfDeployedInstances(String requiredProcessingUnitName, int minimumNumberOfDeployedInstances) {
        
        IT dependency = createDependency(requiredProcessingUnitName);
        dependency.setMinimumNumberOfDeployedInstances(minimumNumberOfDeployedInstances);
        addDependency(dependency);
        return this;
    }

    public AbstractProcessingUnitDependenciesConfigurer<T,IT,S,IS> dependsOnMinimumNumberOfDeployedInstancesPerPartition(String requiredProcessingUnitName, int minimumNumberOfDeployedInstances) {
        
        IT dependency = createDependency(requiredProcessingUnitName);
        dependency.setMinimumNumberOfDeployedInstancesPerPartition(minimumNumberOfDeployedInstances);
        addDependency(dependency);
        return this;
    }

    public AbstractProcessingUnitDependenciesConfigurer<T,IT,S,IS> dependsOnDeployed(String requiredProcessingUnitName) {
        IT dependency = createDependency(requiredProcessingUnitName);
        dependency.setWaitForDeploymentToComplete(true);
        addDependency(dependency);
        return this;
    }

    protected IT createDependency(String requiredProcessingUnitName) {
        return dependencyFactory.create(requiredProcessingUnitName);
    }

    @SuppressWarnings("unchecked") // IT extends T
    protected void addDependency(IT dependency) {
        dependencies.addDependency((T)dependency);
    }
}
