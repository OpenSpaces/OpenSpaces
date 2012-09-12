package org.openspaces.grid.gsm.machines.isolation;

import org.openspaces.admin.internal.pu.elastic.ElasticMachineIsolationConfig;

public class ElasticProcessingUnitMachineIsolationFactory {
    
    public ElasticProcessingUnitMachineIsolation create(String processingUnitName, ElasticMachineIsolationConfig isolationConfig) {
        
        if (isolationConfig == null) {
            throw new IllegalStateException("isolationConfig cannot be null");
        }
        
        ElasticProcessingUnitMachineIsolation isolation;
        
        if (isolationConfig.isDedicatedIsolation()) {
            isolation = new DedicatedMachineIsolation(processingUnitName);
        }
        else if (isolationConfig.isSharedIsolation()) {
            isolation = new SharedMachineIsolation(isolationConfig.getSharingId());
        } else if (isolationConfig.isPublicMachineIsolation()) {
            isolation = new PublicMachineIsolation();
        }
        else {
            throw new IllegalStateException("unsupported PU isolation");
        }
        return isolation;
        
    }
}
