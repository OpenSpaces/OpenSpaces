package org.openspaces.pu.container.spi;

import org.openspaces.pu.container.ProcessingUnitContainer;
import org.springframework.context.ApplicationContext;

/**
 * @author kimchy
 */
public interface ApplicationContextProcessingUnitContainer extends ProcessingUnitContainer {

    ApplicationContext getApplicationContext();
}
