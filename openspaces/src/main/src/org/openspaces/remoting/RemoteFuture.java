package org.openspaces.remoting;

import org.springframework.dao.DataAccessException;
import org.springframework.remoting.RemoteAccessException;

/**
 * @author kimchy
 */
public interface RemoteFuture<V> {

    void cancel() throws RemoteAccessException, DataAccessException;

    boolean isCancelled();

    V get() throws Exception;

    V get(long timeout) throws Exception;
}
