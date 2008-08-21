package org.openspaces.interop;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertiesAware;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.DeployableProcessingUnitContainerProvider;
import org.openspaces.pu.container.ProcessingUnitContainer;

import java.io.File;

/**
 * @author kimchy
 */
public class DotnetProcessingUnitContainerProvider implements DeployableProcessingUnitContainerProvider, ClusterInfoAware, BeanLevelPropertiesAware {


    private ClusterInfo clusterInfo;

    private BeanLevelProperties beanLevelProperties;

    public void setDeployPath(File deployPath) {
    }

    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    public ProcessingUnitContainer createContainer() throws CannotCreateContainerException {
        
        String deployPath = beanLevelProperties.getContextProperties().getProperty(CONTEXT_PROPERTY_DEPLOY_PATH);
        DotnetProcessingUnitContainer dotnetpuContainer = new DotnetProcessingUnitContainer(deployPath, clusterInfo, beanLevelProperties.getContextProperties());
        
        return dotnetpuContainer;
    }

}
