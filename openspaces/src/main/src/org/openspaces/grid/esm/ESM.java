package org.openspaces.grid.esm;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * The Elastic Service Manager (ESM) provides the support to deploy, manage and
 * monitor elastic services in the Grid/Cloud.
 */
public interface ESM extends com.gigaspaces.grid.esm.ESM {

    /**
     * Returns the elastic configuration of this processing unit
     * 
     * This method is only available if the processing unit deployment is elastic.
     * This method might be removed in future versions, since it may expose PII (such as passwords).
     * 
     * @since 8.0
     */
    Map<String, String> getProcessingUnitElasticProperties(String processingUnitName) throws RemoteException;

    void setProcessingUnitElasticProperties(String processingUnitName, Map<String, String> properties) throws RemoteException;
}
