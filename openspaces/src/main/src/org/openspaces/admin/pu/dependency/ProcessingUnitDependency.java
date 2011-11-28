package org.openspaces.admin.pu.dependency;

/**
 * Defines dependency between an unspecified dependant processing unit and one required processing unit.
 * 
 * @author itaif
 * @since 8.0.6
 */
public interface ProcessingUnitDependency {

    /**
     * @return the name of the required processing unit (the processing unit that is being dependent-upon)
     * @since 8.0.6
     */
    String getRequiredProcessingUnitName();
    
    /**
     * @return true if the dependent processing unit waits until the required processing unit deployment is complete. False indicates this dependency is disabled.
     * @since 8.0.6
     */
    boolean getWaitForDeploymentToComplete();
    
    /**
     * @return the number of required processing unit instances per partition that the dependent processing unit waits for. Zero indicates this dependency is disabled.
     * @since 8.0.6
     */
    int getMinimumNumberOfDeployedInstancesPerPartition();
    
    /**
     * @return the number of required processing unit instances that the dependent processing unit waits for. Zero indicates this dependency is disabled.
     * @since 8.0.6
     */
    int getMinimumNumberOfDeployedInstances();
   
}
