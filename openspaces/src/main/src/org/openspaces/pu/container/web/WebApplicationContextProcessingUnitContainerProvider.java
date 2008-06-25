package org.openspaces.pu.container.web;

import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider;

import java.io.File;

/**
 * @author kimchy
 */
public interface WebApplicationContextProcessingUnitContainerProvider extends ApplicationContextProcessingUnitContainerProvider {

    public static final String CLUSTER_INFO_CONTEXT = "clusterInfo";

    public static final String BEAN_LEVEL_PROPERTIES_CONTEXT = "beanLevelProperties";

    public static final String APPLICATION_CONTEXT_CONTEXT = "applicationContext";
    
    void setWarPath(File warPath);

    void setWarTempPath(File warTempPath);
}
