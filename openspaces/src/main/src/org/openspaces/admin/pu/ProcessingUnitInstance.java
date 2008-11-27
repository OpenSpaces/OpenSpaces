package org.openspaces.admin.pu;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.container.jee.JeeProcessingUnitServiceDetails;
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

    /**
     * Returns <code>true</code> if there are embedded spaces started within this processing
     * unit.
     */
    boolean hasEmbeddedSpaces();

    /**
     * Returns a space instance that was started within the processing unit instnace. Will
     * return <code>null</code> if no embedded space instances were started.
     */
    SpaceInstance getSpaceInstance();

    /**
     * Returns all the space instnaces that were stared within the processing unit instnace.
     * Will return an empty array if no space instances were started within this processing unit.
     */
    SpaceInstance[] getSpaceInstances();

    /**
     * Returns <code>true</code> if this processing unit is a jee processing unit.
     */
    boolean isJee();

    /**
     * Returns the jee service details of the jee container that was started within this processign unit.
     */
    JeeProcessingUnitServiceDetails getJeeDetails();
}
