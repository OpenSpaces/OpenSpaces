package org.openspaces.admin.internal.pu.dependency;

import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;

/**
 * Defines dependency between one processing unit and multiple required processing units.
 * 
 * @author itaif
 * @since 8.0.6
 */
public interface ProcessingUnitDetailedDependencies<T extends ProcessingUnitDependency> {

    /**
     * @return true if there are no dependencies on other processing units
     * @since 8.0.6
     */
    boolean isEmpty();

    /**
     * @return The different dependencies comprising this object.   
     * @since 8.0.6
     */
    String[] getRequiredProcessingUnitsNames();
    
    /**
     * @return The processing unit dependency on the specified required processing unit, or null if such dependency does not exist.
     * @since 8.0.6
     */
    T getDependencyByName(String requiredProcessingUnitName);
}
