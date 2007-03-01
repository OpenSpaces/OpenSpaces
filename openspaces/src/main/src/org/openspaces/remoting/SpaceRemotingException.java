package org.openspaces.remoting;

import org.openspaces.core.GigaSpaceException;

/**
 * A general Space remoting exception.
 *
 * @author kimchy
 */
public class SpaceRemotingException extends GigaSpaceException {

    public SpaceRemotingException(String message) {
        super(message);
    }

    public SpaceRemotingException(String message, Throwable cause) {
        super(message, cause);
    }
}
