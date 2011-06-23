package org.openspaces.admin.internal.gateway;

import org.openspaces.admin.gateway.BootstrapResult;

public class DefaultBootstrapResult implements BootstrapResult {

    private final Throwable failureCause;
    
    private DefaultBootstrapResult(Throwable failureCause) {
        this.failureCause = failureCause;
    }
    
    public DefaultBootstrapResult() {
        this(null);
    }

    public static BootstrapResult getFailedResult(Throwable failureReason) {
        return new DefaultBootstrapResult(failureReason);
    }

    public boolean isSucceeded() {
        return !isFailed();
    }

    public boolean isFailed() {
        return failureCause != null;
    }

    public Throwable getFailureCause() {
        return failureCause;
    }

}
