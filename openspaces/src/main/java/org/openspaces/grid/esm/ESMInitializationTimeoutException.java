package org.openspaces.grid.esm;

import java.util.concurrent.TimeUnit;

public class ESMInitializationTimeoutException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ESMInitializationTimeoutException(long timeout, TimeUnit timeUnit) {
        super("Esm initialization took more than " + timeout + " " + timeUnit);
    }
}
