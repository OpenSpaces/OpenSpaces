package org.openspaces.grid.esm;

import java.rmi.RemoteException;
import java.util.Map;

import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.grid.gsm.strategy.ElasticScaleStrategyEvents;

/**
 * The Elastic Service Manager (ESM) provides the support to deploy, manage and
 * monitor elastic services in the Grid/Cloud.
 */
public interface ESM extends com.gigaspaces.grid.esm.ESM {

    void setProcessingUnitElasticProperties(String processingUnitName, Map<String, String> properties) throws RemoteException;

    void setProcessingUnitScaleStrategy(String puName, ScaleStrategyConfig scaleStrategyConfig) throws RemoteException;

    ScaleStrategyConfig getProcessingUnitScaleStrategyConfig(String processingUnitName) throws RemoteException;
    
    ElasticScaleStrategyEvents getScaleStrategyEvents(final long cursor, final int maxNumberOfEvents) throws RemoteException;
}
