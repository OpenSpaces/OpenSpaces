package org.openspaces.pu.container.web.context;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;

/**
 * @author kimchy
 */
public class ProcessingUnitContextLoaderListener extends ContextLoaderListener {

    protected ContextLoader createContextLoader() {
        return new ProcessingUnitContextLoader();
    }
}
