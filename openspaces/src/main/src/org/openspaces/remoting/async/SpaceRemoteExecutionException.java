package org.openspaces.remoting.async;

import java.util.concurrent.ExecutionException;

/**
 * An extension for {@link java.util.concurrent.ExecutionException} holding both the
 * {@link SpaceRemoteInvocation remoteInvocation} and the
 * {@link SpaceRemoteResult} remote result.
 *
 * @author kimchy
 */
public class SpaceRemoteExecutionException extends ExecutionException {

    private SpaceRemoteInvocation remoteInvocation;

    private SpaceRemoteResult remoteResult;

    public SpaceRemoteExecutionException(SpaceRemoteInvocation remoteInvocation, String message, Throwable cause) {
        super(message, cause);
        this.remoteInvocation = remoteInvocation;
    }

    public SpaceRemoteExecutionException(SpaceRemoteInvocation remoteInvocation, SpaceRemoteResult remoteResult) {
        super("Remote Invocation failed with invocation [" + remoteInvocation + "]", remoteResult.getEx());
        this.remoteInvocation = remoteInvocation;
        this.remoteResult = remoteResult;
    }

    /**
     * Returns the remote invocation that caused this execution exception.
     */
    public SpaceRemoteInvocation getRemoteInvocation() {
        return remoteInvocation;
    }

    /**
     * Returns the remote result that caused this execution exception.
     */
    public SpaceRemoteResult getRemoteResult() {
        return remoteResult;
    }
}
