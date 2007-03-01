package org.openspaces.remoting;

/**
 * @author kimchy
 */
public interface RemoteFuture {

    void cancel() throws SpaceRemotingException;

    boolean isCancelled();

    Object get() throws Exception;

    Object get(long timeout) throws Exception;
}
