package org.openspaces.admin.internal.esm;

import java.util.Map;

import net.jini.core.lookup.ServiceID;

import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * @author Moran Avigdor
 * @author itaif
 */
public interface InternalElasticServiceManager extends ElasticServiceManager, InternalAgentGridComponent  {

    ServiceID getServiceID();

    /**
     * Returns the elastic configuration of this processing unit
     * 
     * This method is only available if the processing unit deployment is elastic.
     * This method might be removed in future versions, since it may expose PII (such as passwords).
     * 
     * @since 8.0
     */
    Map<String,String> getProcessingUnitElasticProperties(ProcessingUnit pu);

    void setProcessingUnitElasticProperties(ProcessingUnit pu, Map<String,String> properties);
}