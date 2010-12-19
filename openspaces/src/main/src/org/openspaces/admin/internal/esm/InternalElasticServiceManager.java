package org.openspaces.admin.internal.esm;

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

    ProcessingUnitElasticConfig getProcessingUnitElasticConfig(ProcessingUnit pu);

    void setProcessingUnitElasticConfig(ProcessingUnit pu, ProcessingUnitElasticConfig properties);
}