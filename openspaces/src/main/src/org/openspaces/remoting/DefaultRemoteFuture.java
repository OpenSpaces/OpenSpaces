package org.openspaces.remoting;

import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;
import org.springframework.remoting.RemoteAccessException;

/**
 * @author kimchy
 */
public class DefaultRemoteFuture<T> implements RemoteFuture<T> {

    private GigaSpace gigaSpace;

    private SpaceRemoteInvocation remoteInvocation;

    private boolean cancelled;

    private SpaceRemoteResult<T> remoteResult;

    private SpaceRemoteResult<T> template;

    public DefaultRemoteFuture(GigaSpace gigaSpace, SpaceRemoteInvocation remoteInvocation) {
        this.gigaSpace = gigaSpace;
        this.remoteInvocation = remoteInvocation;
        this.template = new SpaceRemoteResult<T>(remoteInvocation);
    }

    public void cancel() throws RemoteAccessException, DataAccessException {
        Object retVal = gigaSpace.take(remoteInvocation, 0);
        if (retVal != null) {
            cancelled = true;
        }
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public T get() throws Exception {
        T retVal = handleResult();
        if (retVal != null) {
            return retVal;
        }
        remoteResult = gigaSpace.take(template, Integer.MAX_VALUE);
        return handleResult();
    }

    public T get(long timeout) throws Exception {
        T retVal = handleResult();
        if (retVal != null) {
            return retVal;
        }
        remoteResult = gigaSpace.take(template, timeout);
        return handleResult();
    }

    private T handleResult() throws Exception {
        if (remoteResult == null) {
            return null;
        }
        if (remoteResult.getEx() != null) {
            throw remoteResult.getEx();
        }
        return remoteResult.getResult();
    }
}
