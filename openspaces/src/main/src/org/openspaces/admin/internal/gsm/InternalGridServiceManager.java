package org.openspaces.admin.internal.gsm;

import java.util.Map;

import net.jini.core.lookup.ServiceID;

import org.jini.rio.monitor.ProvisionMonitorAdmin;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;

import com.gigaspaces.grid.gsm.GSM;

/**
 * @author kimchy
 * @author itaif (ESM proxy methods)
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
    
    void setProcessingUnitElasticProperties(ProcessingUnit pu, Map<String,String> properties);
    
    void setProcessingUnitScaleStrategyConfig(ProcessingUnit pu, ScaleStrategyConfig scaleStratefyConfig);

    /** Used as a call back by the esm to update the store of records of elastic properties on the actual gsm*/
    void updateProcessingUnitElasticPropertiesOnGsm(ProcessingUnit pu, Map<String, String> elasticProperties);

    //TODO: Replace this method with a push notification each time scale strategy config changes.
    ScaleStrategyConfig getProcessingUnitScaleStrategyConfig(ProcessingUnit pu);
}