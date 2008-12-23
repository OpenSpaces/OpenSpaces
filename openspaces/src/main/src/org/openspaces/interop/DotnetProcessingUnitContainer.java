package org.openspaces.interop;

import com.j_spaces.core.IJSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.pu.container.CannotCloseContainerException;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;

/**
 * @author kimchy
 * @since 6.6
 */
public class DotnetProcessingUnitContainer implements ProcessingUnitContainer, ServiceDetailsProvider {
       
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
    
    public IJSpace[] getSpaces() {
        return dotnetProcessingUnitBean.getSpaces();
    }

    public ServiceDetails[] getServicesDetails() {
        ServiceDetails[] details = dotnetProcessingUnitBean.getServicesDetails();
        if (details != null) {
            for (ServiceDetails detail : details) {
                if (detail instanceof DotnetContainerServiceDetails) {
                    ((DotnetContainerServiceDetails) detail).setType("pure");
                }
            }
        }
        return details;
    }
}
