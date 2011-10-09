package org.openspaces.grid.esm;

import java.rmi.RemoteException;
import java.util.Map;

import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;

/**
 * The Elastic Service Manager (ESM) provides the support to deploy, manage and
 * monitor elastic services in the Grid/Cloud.
 */
public interface ESM extends com.gigaspaces.grid.esm.ESM {

    void setProcessingUnitElasticProperties(String processingUnitName, Map<String, String> properties) throws RemoteException;

    void setProcessingUnitScaleStrategy(String puName, ScaleStrategyConfig scaleStrategyConfig) throws RemoteException;

    ScaleStrategyConfig getProcessingUnitScaleStrategyConfig(String processingUnitName) throws RemoteException;

    /**
     * @return true if the scale strategy of the specified processing unit is being monitored and corrective actions are enforced.
     * In case the processing unit has been un-deployed returns true as long as the containers and machines shutdown is in progress.
     * @since 8.0.5
     */
    boolean isManagingProcessingUnit(String processingUnitName) throws RemoteException;

    /**
     * @return true if the processing unit is managed by the ESM, and the scale SLA has been reached.
     * In case the processing unit has been un-deployed returns false.
     * @since 8.0.5
     */
    boolean isManagingProcessingUnitAndScaleNotInProgress(String processingUnitName)throws RemoteException;
}
