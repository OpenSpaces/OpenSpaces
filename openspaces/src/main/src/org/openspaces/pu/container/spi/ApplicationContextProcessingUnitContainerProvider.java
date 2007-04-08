package org.openspaces.pu.container.spi;

import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.properties.BeanLevelPropertiesAware;
import org.openspaces.pu.container.ProcessingUnitContainerProvider;

import java.io.IOException;

/**
 * @author kimchy
 */
public interface ApplicationContextProcessingUnitContainerProvider extends ProcessingUnitContainerProvider,
        ClusterInfoAware, BeanLevelPropertiesAware {

    static final String DEFAULT_PU_CONTEXT_LOCATION = "classpath*:/META-INF/spring/pu.xml";
    
    void addConfigLocation(String configLocation) throws IOException;
}
