package org.openspaces.admin.pu;

import org.openspaces.admin.gsm.GridServiceManager;

/**
 * @author kimchy
 */
public interface ProcessingUnit extends Iterable<ProcessingUnitInstance> {

    String getName();

    int getNumberOfInstances();

    int getNumberOfBackups();

    DeploymentStatus getStatus();

    /**
     * Returns <code>true</code> if there is a managing GSM for it.
     */
    boolean isManaged();

    GridServiceManager getManagingGridServiceManager();

    GridServiceManager[] getBackupGridServiceManagers();

    GridServiceManager getBackupGridServiceManager(String gridServiceManagerUID);

    ProcessingUnitInstance[] getInstances();
}
