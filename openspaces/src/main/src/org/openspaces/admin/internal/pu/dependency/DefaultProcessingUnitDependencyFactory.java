package org.openspaces.admin.internal.pu.dependency;

public class DefaultProcessingUnitDependencyFactory implements InternalProcessingUnitDependencyFactory<InternalProcessingUnitDependency> {

    @Override
    public InternalProcessingUnitDependency create(String requiredProcessingUnitName) {
        return new DefaultProcessingUnitDependency(requiredProcessingUnitName);
    }

}
