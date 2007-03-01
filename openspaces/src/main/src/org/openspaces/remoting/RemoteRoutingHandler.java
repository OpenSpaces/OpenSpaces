package org.openspaces.remoting;

/**
 * A general interface allowing for pluggable computation of the remoting
 * invocation routing field. Routing field controls the partition the invocation
 * will be directed to when working with a partitioned space.
 *
 * @author kimchy
 */
public interface RemoteRoutingHandler {

    /**
     * Sets the routing field using {@link org.openspaces.remoting.SpaceRemoteInvocation#setRouting(Integer)}
     * based on the remoting invocation.
     */
    void setRemoteInvocationRouting(SpaceRemoteInvocation remoteInvocation);
}
