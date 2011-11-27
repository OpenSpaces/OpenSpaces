package org.openspaces.admin.internal.gsm;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.jini.core.lookup.ServiceID;

import org.jini.rio.monitor.ProvisionMonitorAdmin;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;

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

    /**
     * Decrements a planned instance if the number of planned instances is less then the actual instances to maintain.
     * @return <code>true</code> if a planned instance was successfully decremented; <code>false</code> planned instances hasn't changed.
     * @since 8.0.4
     */
    boolean decrementPlannedInstances(ProcessingUnit processingUnit);
    
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

    boolean isManagedByElasticServiceManager(ProcessingUnit pu);

    boolean isManagedByElasticServiceManagerAndScaleNotInProgress(ProcessingUnit pu);

     boolean undeployProcessingUnitsAndWait(ProcessingUnit[] processingUnits, long timeout, TimeUnit timeUnit);

    ProcessingUnit deploy(Application application, ProcessingUnitDeploymentTopology deploymentTopology, long timeout, TimeUnit timeUnit);
}