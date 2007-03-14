package org.openspaces.remoting;

import net.jini.core.entry.Entry;

/**
 * Represents a Space remote inocation result. Either holds the remote inocation result or and
 * exception that occured during the invocation.
 * 
 * @author kimchy
 */
public class SpaceRemoteResult<T> implements Entry {

    private static final long serialVersionUID = -5466117072163590804L;

    public String invocationId;

    public T result;

    public Exception ex;

    public Integer routing;

    /**
     * Constructs a new remote invocation result.
     */
    public SpaceRemoteResult() {

    }

    /**
     * Constructs a new remote invocation result based on the remote invocation. Uses the remote
     * invocation to initalize the {@link #invocationId} and {@link #routing} (acts as the
     * correlation ids).
     */
    public SpaceRemoteResult(SpaceRemoteInvocation remoteInvocation) {
        this.invocationId = remoteInvocation.__getEntryInfo().m_UID;
        this.routing = remoteInvocation.routing;
    }

    /**
     * Constructs a new remote invocation result based on the remote invocation and an exception.
     * Uses the remote invocation to initalize the {@link #invocationId} and {@link #routing} (acts
     * as the correlation ids).
     */
    public SpaceRemoteResult(SpaceRemoteInvocation remoteInvocation, Exception ex) {
        this.invocationId = remoteInvocation.__getEntryInfo().m_UID;
        this.ex = ex;
        this.routing = remoteInvocation.routing;
    }

    /**
     * Constructs a new remote invocation result based on the remote invocation and a result. Uses
     * the remote invocation to initalize the {@link #invocationId} and {@link #routing} (acts as
     * the correlation ids).
     */
    public SpaceRemoteResult(SpaceRemoteInvocation remoteInvocation, T result) {
        this.invocationId = remoteInvocation.__getEntryInfo().m_UID;
        this.result = result;
        this.routing = remoteInvocation.routing;
    }

    /**
     * Returns the result of the remote invocation. Can be <code>null</code> if the remote service
     * methods has a void return value or if an exception occured during the invocation.
     */
    public T getResult() {
        return result;
    }

    /**
     * Returns an exception that was thrown from the remote service method. Can be <code>null</code>
     * if no exception was thrown.
     */
    public Exception getEx() {
        return ex;
    }

    public static String[] __getSpaceIndexedFields() {
        return new String[] { "routing", "invocationId" };
    }
}
