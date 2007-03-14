package org.openspaces.pu.container.spi;

import org.openspaces.pu.container.ProcessingUnitContainer;
import org.springframework.context.ApplicationContext;

/**
 * A processing unit container that is based on Spring {@link ApplicationContext}.
 * 
 * @author kimchy
 */
public interface ApplicationContextProcessingUnitContainer extends ProcessingUnitContainer {

    ApplicationContext getApplicationContext();
}
