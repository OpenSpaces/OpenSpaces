package org.openspaces.admin.internal.pu.dependency;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jini.rio.core.RequiredDependencies;
import org.jini.rio.core.RequiredDependency;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;

public abstract class AbstractProcessingUnitDetailedDependencies<T extends ProcessingUnitDependency, IT extends InternalProcessingUnitDependency> 
    implements InternalProcessingUnitDetailedDependencies<T,IT> {

    private final List<IT> deploymentDependencies;
    private final InternalProcessingUnitDependencyFactory<IT> dependencyFactory;
    
    public AbstractProcessingUnitDetailedDependencies(InternalProcessingUnitDependencyFactory<IT> dependencyFactory) {
        this.deploymentDependencies = new LinkedList<IT>();
        this.dependencyFactory = dependencyFactory;
    }
    
    @Override
    public boolean isEmpty() {
        return deploymentDependencies.isEmpty();
    }

    @Override
    public String[] getRequiredProcessingUnitsNames() {
        List<String> names = new ArrayList<String>(deploymentDependencies.size());
        
        for (IT dependency : deploymentDependencies) {
            names.add(dependency.getRequiredProcessingUnitName());
        }
        return names.toArray(new String[deploymentDependencies.size()]);
    }

    @SuppressWarnings("unchecked") // IT extends T
    @Override
    public void addDependency(T newDependency) {
        String requiredProcessingUnitName = newDependency.getRequiredProcessingUnitName();
        IT existingDependency = (IT) getDependencyByName(requiredProcessingUnitName);
        if (existingDependency != null) {
            existingDependency.mergeDependency(newDependency);
        }
        else {
            deploymentDependencies.add((IT)newDependency);
        }
    }

    @SuppressWarnings("unchecked") // IT extends T
    @Override
    public T getDependencyByName(String requiredProcessingUnitName) {
        for (IT dependency : deploymentDependencies) {
            if (requiredProcessingUnitName.equals(dependency.getRequiredProcessingUnitName())) {
                return (T)dependency;
            }
        }
        return null;
    }
    
    @Override
    public RequiredDependencies toRequiredDependencies() {
        
        RequiredDependencies requiredDependencies = new RequiredDependencies();
        for (IT dependency : deploymentDependencies){
            requiredDependencies.addRequiredDependency(dependency.toRequiredDependency());
        }
        return requiredDependencies;
    }

    @SuppressWarnings("unchecked") // IT extends T
    @Override
    public void addDependencies(RequiredDependencies requiredDependencies) {
        for (final String requiredDependencyName : requiredDependencies.getRequiredDependenciesNames()){
            final RequiredDependency requiredDependency = requiredDependencies.getRequiredDependencyByName(requiredDependencyName);
            final IT dependency = dependencyFactory.create(requiredDependency.getRequiredProcessingUnitName());
            dependency.mergeDependency(requiredDependency);
            addDependency((T)dependency);
        }
    }

}
