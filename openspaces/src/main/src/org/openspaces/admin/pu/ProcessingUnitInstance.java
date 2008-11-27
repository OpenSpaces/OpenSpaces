package org.openspaces.admin.pu;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.service.ProcessingUnitServiceDetails;

/**
 * @author kimchy
 */
public interface ProcessingUnitInstance extends GridComponent, Iterable<ProcessingUnitServiceDetails> {

    int getInstanceId();

    int getBackupId();

    ProcessingUnit getProcessingUnit();

    ClusterInfo getClusterInfo();

    GridServiceContainer getGridServiceContainer();

    ProcessingUnitServiceDetails[] getServiceDetails();
}
