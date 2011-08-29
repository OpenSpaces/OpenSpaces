package org.openspaces.admin.internal.gsc;

import java.rmi.RemoteException;

import com.gigaspaces.grid.gsc.GSC;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @author kimchy
 */
public interface InternalGridServiceContainer extends GridServiceContainer, InternalAgentGridComponent {

    ServiceID getServiceID();

    GSC getGSC();

    void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance);

    void removeProcessingUnitInstance(String uid);
    
    /**
     * @return false only when all processing unit instances have completed shutdown.
     * @see ProcessingUnitInstanceRemovedEventListener - use this event to get an indication when processing unit instance has started shutdown.
     */
    boolean hasProcessingUnitInstances() throws RemoteException;
    
}