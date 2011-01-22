package org.openspaces.grid.esm;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * The Elastic Service Manager (ESM) provides the support to deploy, manage and
 * monitor elastic services in the Grid/Cloud.
 */
public interface ESM extends com.gigaspaces.grid.esm.ESM {

    void setProcessingUnitElasticProperties(String processingUnitName, Map<String, String> properties) throws RemoteException;

    void setScaleStrategy(String puName, String strategyClassName, Map<String,String> strategyProperties) throws RemoteException;
}
