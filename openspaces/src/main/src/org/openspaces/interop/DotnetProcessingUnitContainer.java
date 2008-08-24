package org.openspaces.interop;

import java.util.Properties;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.pu.container.CannotCloseContainerException;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;

/**
 * @author kimchy
 * @since 6.6
 */
public class DotnetProcessingUnitContainer implements ProcessingUnitContainer {
       
    private DotnetProcessingUnitBean dotnetProcessingUnitBean;
    
    public DotnetProcessingUnitContainer(ClusterInfo clusterInfo, BeanLevelProperties beanLevelProperties) throws CannotCreateContainerException{       
        dotnetProcessingUnitBean = new DotnetProcessingUnitBean();
        dotnetProcessingUnitBean.setClusterInfo(clusterInfo);
        dotnetProcessingUnitBean.setBeanLevelProperties(beanLevelProperties);
        try {
            dotnetProcessingUnitBean.afterPropertiesSet();
        } catch (Exception e) {
            throw new CannotCreateContainerException(e.getMessage(), e);
        } 
    }

    public void close() throws CannotCloseContainerException {
        try {
            dotnetProcessingUnitBean.destroy();
        } catch (Exception e) {
            throw new CannotCloseContainerException(e.getMessage(), e);
        }
    }
    
}
