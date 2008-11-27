package org.openspaces.admin.pu;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.core.cluster.ClusterInfo;

/**
 * @author kimchy
 */
public interface ProcessingUnitInstance {

    String getUID();

    ProcessingUnit getProcessingUnit();

    ClusterInfo getClusterInfo();

    GridServiceContainer getGridServiceContainer();
}
