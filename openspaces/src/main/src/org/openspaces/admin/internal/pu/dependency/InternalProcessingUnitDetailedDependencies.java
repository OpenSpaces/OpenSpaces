package org.openspaces.admin.internal.pu.dependency;

import org.jini.rio.core.RequiredDependencies;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;

/*
 * @since 8.0.6
 * @author itaif
 */
    
public interface InternalProcessingUnitDetailedDependencies<T extends ProcessingUnitDependency, IT extends InternalProcessingUnitDependency> 
    extends ProcessingUnitDetailedDependencies<T> {
    
    /**
     * Adds the specified dependency.
     * If a with the same name exits, then the old dependency is merged with the new dependency.
     * 
     * @since 8.0.6
     */
    void addDependency(T dependency);
        
    /**
     * Converts this to GSM compatible format.
     * 
     * @since 8.0.6
     */
    RequiredDependencies toRequiredDependencies();
    
    /**
     * Merges with dependencies in GSM format
     */
    void addDependencies(RequiredDependencies requiredDependencies);

    String getCommandLineOption();
}
