package org.openspaces.remoting;

/**
 * A remoting result reducer (ala Map Reduce) used when working with
 * {@link org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean} in
 * broadcast mode in order to reduce the broadcast results into a "client
 * side" result value.
 *
 * @author kimchy
 */
public interface RemoteResultReducer<T> {

    /**
     * Reduces a list of Space remoting invocation results to an Object value. Can use
     * the provided remote invocation to perform different reduce operations, for example
     * based on the {@link SpaceRemotingInvocation#getMethodName()}.
     *
     * <p>An exception thrown from the reduce operation will be propagated to the client.
     *
     * @param results            A list of results from (usually) broadcast sync remote invocation.
     * @param remotingInvocation The remote invocation
     * @return A reduced return value (to the calling client)
     * @throws Exception An exception that will be propagated to the client
     */
    T reduce(SpaceRemotingResult[] results, SpaceRemotingInvocation remotingInvocation) throws Exception;
}
