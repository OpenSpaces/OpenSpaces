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

    void setProcessingUnitElasticProperties(ProcessingUnit pu, Map<String,String> properties);
    
    void setScaleStrategy(ProcessingUnit pu, String strategyClassName, Map<String,String> strategyProperties);
}