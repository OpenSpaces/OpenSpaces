package org.openspaces.remoting;

import org.openspaces.core.GigaSpaceException;

/**
 * A general Space remoting exception.
 *
 * @author kimchy
 */
public class SpaceRemotingException extends GigaSpaceException {

    private static final long serialVersionUID = -3895585887561945840L;

    public SpaceRemotingException(String message) {
        super(message);
    }

    public SpaceRemotingException(String message, Throwable cause) {
        super(message, cause);
    }
}
