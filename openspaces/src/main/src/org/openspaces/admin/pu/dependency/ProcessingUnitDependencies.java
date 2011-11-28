package org.openspaces.admin.pu.dependency;

/**
 * Aggregates all of the processing unit lifecycle stages that can be postponed.
 * For XAP processing units it includes postponing of processing unit instance deployment.
 * Cloudify extends this interface to include also the start stage of the processing unit instance.  
 * 
 * @author itaif
 * @since 8.0.6
 * @param <T> The life cycle of the required processing unit. This is an extension point for Cloudify that has a more elaborate startup sequence and allows depending on different startup stages of the required processing unit.
 */
public interface ProcessingUnitDependencies<T extends ProcessingUnitDependency> {
    
    ProcessingUnitDeploymentDependencies<T> getDeploymentDependencies();
    
    /**
     * @return the names of all required processing units (All processing units that are dependent upon)
     */
    String[] getDependenciesRequiredProcessingUnitNames();
}
