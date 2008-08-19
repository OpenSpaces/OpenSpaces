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

    private File deployPath;

    private ClusterInfo clusterInfo;

    private BeanLevelProperties beanLevelProperties;

    public void setDeployPath(File deployPath) {
        this.deployPath = deployPath;
    }

    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    public ProcessingUnitContainer createContainer() throws CannotCreateContainerException {
        // TODO yada yada yada
        return new DotnetProcessingUnitContainer();
    }

}
