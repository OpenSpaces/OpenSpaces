package org.openspaces.remoting;

/**
 * @author kimchy
 */
public interface RemoteRoutingHandler {

    void setRemoteInvocationRouting(SpaceRemoteInvocation remoteInvocation);
}
