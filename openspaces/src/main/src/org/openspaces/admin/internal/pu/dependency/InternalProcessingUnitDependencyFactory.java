package org.openspaces.admin.internal.pu.dependency;

public interface InternalProcessingUnitDependencyFactory<IT extends InternalProcessingUnitDependency>{
    
    IT create(String requiredProcessingUnitName);
    
}
