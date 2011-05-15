package org.openspaces.admin.application;

import org.openspaces.admin.pu.ProcessingUnits;

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
