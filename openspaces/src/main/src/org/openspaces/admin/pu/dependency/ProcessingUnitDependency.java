package org.openspaces.admin.pu.dependency;

/**
 * Defines dependency between an unspecified dependant processing unit and one required processing unit.
 * 
 * @author itaif
 * @since 8.0.6
 */
public interface ProcessingUnitDependency {

    String getRequiredProcessingUnitName();
    
    boolean getWaitForDeploymentToComplete();
    
    int getMinimumNumberOfDeployedInstancesPerPartition();
    
    int getMinimumNumberOfDeployedInstances();
   
}
