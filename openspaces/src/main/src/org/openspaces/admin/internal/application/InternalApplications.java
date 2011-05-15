package org.openspaces.admin.internal.application;

import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.Applications;
import org.openspaces.admin.pu.ProcessingUnit;

public interface InternalApplications extends Applications  {

    void addApplication(Application application, ProcessingUnit processingUnit);

    void removeProcessingUnit(ProcessingUnit processingUnit);
}
