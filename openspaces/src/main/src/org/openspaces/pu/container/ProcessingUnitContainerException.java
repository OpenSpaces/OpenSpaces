package org.openspaces.pu.container;

import org.springframework.core.NestedRuntimeException;

/**
 * A general base class for processing unit container exceptions.
 * 
 * @author kimchy
 */
public abstract class ProcessingUnitContainerException extends NestedRuntimeException {

    public ProcessingUnitContainerException(String msg) {
        super(msg);
    }

    public ProcessingUnitContainerException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
