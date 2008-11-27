package org.openspaces.admin.pu;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.core.cluster.ClusterInfo;

/**
 * @author kimchy
 */
public interface ProcessingUnitInstance {

    String getUID();

    int getInstanceId();

    int getBackupId();

    ProcessingUnit getProcessingUnit();

    ClusterInfo getClusterInfo();

    GridServiceContainer getGridServiceContainer();
}
