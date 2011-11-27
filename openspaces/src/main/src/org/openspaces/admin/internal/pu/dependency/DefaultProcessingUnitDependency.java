package org.openspaces.admin.internal.pu.dependency;

import org.jini.rio.core.RequiredDependency;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;

public class DefaultProcessingUnitDependency implements InternalProcessingUnitDependency {

    RequiredDependency requiredDependency;
    
    public DefaultProcessingUnitDependency(String requiredProcessingUnitName) {
        this(new RequiredDependency(requiredProcessingUnitName));
    }
    
    private DefaultProcessingUnitDependency(RequiredDependency requiredDependency) {
        this.requiredDependency = requiredDependency;
    }

    public boolean getWaitForDeploymentToComplete() {
        return this.requiredDependency.getWaitForDeploymentToComplete();
    }
    
    public void setWaitForDeploymentToComplete(boolean waitForDeploymentToComplete) {
        this.requiredDependency.setWaitForDeploymentToComplete(waitForDeploymentToComplete);
    }

    public int getMinimumNumberOfDeployedInstancesPerPartition() {
        return this.requiredDependency.getMinimumNumberOfDeployedInstancesPerPartition();
    }
    
    public void setMinimumNumberOfDeployedInstancesPerPartition(int minimumNumberOfDeployedInstancesPerPartition) {
        this.requiredDependency.setMinimumNumberOfDeployedInstancesPerPartition(minimumNumberOfDeployedInstancesPerPartition);
    }

    public int getMinimumNumberOfDeployedInstances() {
        return this.requiredDependency.getMinimumNumberOfDeployedInstances();
    }
    
    public void setMinimumNumberOfDeployedInstances(int minimumNumberOfDeployedInstances) {
        this.requiredDependency.setMinimumNumberOfDeployedInstances(minimumNumberOfDeployedInstances);
    }

    @Override
    public String getRequiredProcessingUnitName() {
        return this.requiredDependency.getRequiredProcessingUnitName();
    }

    @Override
    public void mergeDependency(ProcessingUnitDependency otherDependency) {
        mergeDependency(((InternalProcessingUnitDependency)otherDependency).toRequiredDependency());
    }

    @Override
    public void mergeDependency(RequiredDependency otherRequiredDependency) {
        requiredDependency.merge(otherRequiredDependency);
    }

    @Override
    public RequiredDependency toRequiredDependency() {
        return new RequiredDependency(requiredDependency);
    }

}
