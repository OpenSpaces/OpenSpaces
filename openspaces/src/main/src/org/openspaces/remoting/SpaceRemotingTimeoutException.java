package org.openspaces.remoting;

/**
 * A Space remoting exception caused by a timeout waiting for a result.
 *
 * @author kimchy
 */
public class SpaceRemotingTimeoutException extends SpaceRemotingException {

    private long timeout;

    public SpaceRemotingTimeoutException(String message, long timeout) {
        super(message);
        this.timeout = timeout;
    }

    public SpaceRemotingTimeoutException(String message, long timeout, Throwable cause) {
        super(message, cause);
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }
}
