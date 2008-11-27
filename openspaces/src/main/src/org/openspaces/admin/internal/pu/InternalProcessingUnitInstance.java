package org.openspaces.admin.internal.pu;

import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.support.InternalGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @author kimchy
 */
public interface InternalProcessingUnitInstance extends ProcessingUnitInstance, InternalGridComponent {

    ServiceID getServiceID();

    ServiceID getGridServiceContainerServiceID();

    void setProcessingUnit(ProcessingUnit processingUnit);

    void setGridServiceContainer(GridServiceContainer gridServiceContainer);
}
