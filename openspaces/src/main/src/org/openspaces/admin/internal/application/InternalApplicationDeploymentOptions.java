package org.openspaces.admin.internal.application;

import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnitDeployment;

public interface InternalApplicationDeploymentOptions {
    
    String getApplicationName();
    
    ProcessingUnitDeployment[] getProcessingUnitDeployments(Admin admin);
}
