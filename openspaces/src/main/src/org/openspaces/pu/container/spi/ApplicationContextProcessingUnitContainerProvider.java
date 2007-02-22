package org.openspaces.pu.container.spi;

import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.config.BeanLevelPropertiesAware;
import org.openspaces.pu.container.ProcessingUnitContainerProvider;

import java.io.IOException;

/**
 * @author kimchy
 */
public interface ApplicationContextProcessingUnitContainerProvider extends ProcessingUnitContainerProvider,
        ClusterInfoAware, BeanLevelPropertiesAware {

    void addConfigLocation(String configLocation) throws IOException;
}
