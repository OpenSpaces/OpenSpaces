package org.openspaces.remoting;

/**
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
