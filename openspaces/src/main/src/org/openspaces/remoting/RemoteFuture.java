package org.openspaces.remoting;

/**
 * @author kimchy
 */
public interface RemoteFuture<V> {

    void cancel() throws SpaceRemotingException;

    boolean isCancelled();

    V get() throws Exception;

    V get(long timeout) throws Exception;
}
