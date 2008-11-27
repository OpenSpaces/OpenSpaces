package org.openspaces.admin.gsc;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @author kimchy
 */
public interface GridServiceContainer extends GridComponent, Iterable<ProcessingUnitInstance> {

    ProcessingUnitInstance[] getProcessingUnitInsances();
}