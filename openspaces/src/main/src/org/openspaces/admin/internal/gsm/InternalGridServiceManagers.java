package org.openspaces.admin.internal.gsm;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.application.Application;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;

/**
 * @author kimchy
 */
public interface InternalGridServiceManagers extends GridServiceManagers {

    void addGridServiceManager(InternalGridServiceManager gridServiceManager);

    InternalGridServiceManager removeGridServiceManager(String uid);

    /**
     * Replaces the grid service manager, returning the old one
     */
    InternalGridServiceManager replaceGridServiceManager(InternalGridServiceManager gridServiceManager);

    boolean undeployProcessingUnitsAndWait(ProcessingUnit[] processingUnits, long remaining, TimeUnit milliseconds);

    ProcessingUnit deploy(Application application, ProcessingUnitDeploymentTopology puDeployment, long timeout, TimeUnit timeUnit);
}