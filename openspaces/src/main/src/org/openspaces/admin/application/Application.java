package org.openspaces.admin.application;

import org.openspaces.admin.pu.ProcessingUnits;

/**
 * Describes a group of processing units that interact together as an application.
 * 
 * @author itaif
 * @since 8.0.3
 */
public interface Application {

    /**
     * @return the processing units associated with the application
     */
    ProcessingUnits getProcessingUnits();
    
    /**
     * @return the name of the application.
     */
    String getName();

}
