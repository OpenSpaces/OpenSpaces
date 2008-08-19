package org.openspaces.interop;

import org.openspaces.pu.container.CannotCloseContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;

/**
 * @author kimchy
 */
public class DotnetProcessingUnitContainer implements ProcessingUnitContainer {

    public void close() throws CannotCloseContainerException {
        // TODO close down
    }
}
