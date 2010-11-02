package org.openspaces.admin.internal.gsm;

import net.jini.core.lookup.ServiceID;

import org.jini.rio.monitor.ProvisionMonitorAdmin;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import com.gigaspaces.grid.gsm.GSM;

/**
 * @author kimchy
 */
public interface InternalGridServiceManager extends GridServiceManager, InternalAgentGridComponent {

    ServiceID getServiceID();

    GSM getGSM();

    ProvisionMonitorAdmin getGSMAdmin();

    void undeployProcessingUnit(String processingUnitName);

    void destroyInstance(ProcessingUnitInstance processingUnitInstance);

    void decrementInstance(ProcessingUnitInstance processingUnitInstance);

    void incrementInstance(ProcessingUnit processingUnit);

    void relocate(ProcessingUnitInstance processingUnitInstance, GridServiceContainer gridServiceContainer);
    
    public String[] listDeployDir();
}