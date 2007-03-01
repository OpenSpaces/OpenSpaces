package org.openspaces.remoting;

import org.openspaces.core.GigaSpace;

/**
 * @author kimchy
 */
public class DefaultRemoteFuture implements RemoteFuture {

    private GigaSpace gigaSpace;

    private SpaceRemoteInvocation remoteInvocation;

    private boolean cancelled;

    private SpaceRemoteResult remoteResult;

    private SpaceRemoteResult template;

    public DefaultRemoteFuture(GigaSpace gigaSpace, SpaceRemoteInvocation remoteInvocation) {
        this.gigaSpace = gigaSpace;
        this.remoteInvocation = remoteInvocation;
        this.template = new SpaceRemoteResult(remoteInvocation);
    }

    public void cancel() throws SpaceRemotingException {
        Object retVal = gigaSpace.take(remoteInvocation, 0);
        if (retVal != null) {
            cancelled = true;
        }
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public Object get() throws Exception {
        Object retVal = handleResult();
        if (retVal != null) {
            return retVal;
        }
        remoteResult = (SpaceRemoteResult) gigaSpace.take(template, Integer.MAX_VALUE);
        return handleResult();
    }

    public Object get(long timeout) throws Exception {
        Object retVal = handleResult();
        if (retVal != null) {
            return retVal;
        }
        remoteResult = (SpaceRemoteResult) gigaSpace.take(template, timeout);
        return handleResult();
    }

    private Object handleResult() throws Exception {
        if (remoteResult == null) {
            return null;
        }
        if (remoteResult.getEx() != null) {
            throw remoteResult.getEx();
        }
        return remoteResult.getResult();
    }
}
