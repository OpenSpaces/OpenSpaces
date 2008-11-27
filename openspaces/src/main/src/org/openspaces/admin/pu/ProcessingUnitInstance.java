package org.openspaces.admin.pu;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.core.cluster.ClusterInfo;

/**
 * @author kimchy
 */
public interface ProcessingUnitInstance extends GridComponent {

    int getInstanceId();

    int getBackupId();

    ProcessingUnit getProcessingUnit();

    ClusterInfo getClusterInfo();

    GridServiceContainer getGridServiceContainer();
}
