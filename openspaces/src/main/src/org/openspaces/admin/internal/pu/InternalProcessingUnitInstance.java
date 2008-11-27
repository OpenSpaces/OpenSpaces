package org.openspaces.admin.internal.pu;

import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @author kimchy
 */
public interface InternalProcessingUnitInstance extends ProcessingUnitInstance {

    ServiceID getServiceID();

    void setProcessingUnit(ProcessingUnit processingUnit);
}
