package org.openspaces.remoting;

import org.springframework.remoting.RemoteAccessException;

/**
 * A Space remoting exception caused by a timeout waiting for a result.
 * 
 * @author kimchy
 */
public class RemoteTimeoutException extends RemoteAccessException {

    private static final long serialVersionUID = -392552156381478754L;

    private long timeout;

    public RemoteTimeoutException(String message, long timeout) {
        super(message);
        this.timeout = timeout;
    }

    public RemoteTimeoutException(String message, long timeout, Throwable cause) {
        super(message, cause);
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }
}
