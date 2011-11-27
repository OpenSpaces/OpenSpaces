package org.openspaces.admin.pu.dependency;

public interface ProcessingUnitDependencies<T extends ProcessingUnitDependency> {
    
    ProcessingUnitDeploymentDependencies<T> getDeploymentDependencies();
    
    /**
     * @return the names of all required processing units (All processing units that are dependent upon)
     */
    String[] getDependenciesRequiredProcessingUnitNames();
}
