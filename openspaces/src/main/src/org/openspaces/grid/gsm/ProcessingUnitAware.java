package org.openspaces.grid.gsm;

import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.pu.ProcessingUnit;

public interface ProcessingUnitAware {
    
    void setProcessingUnit(ProcessingUnit pu);
    
    void setProcessingUnitSchema(ProcessingUnitSchemaConfig schemaConfig);
}
