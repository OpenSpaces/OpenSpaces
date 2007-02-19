package org.openspaces.events.notify;

import org.openspaces.core.GigaSpaceException;

/**
 * @author kimchy
 */
public class NotifyListenerRegistrationException extends GigaSpaceException {

    public NotifyListenerRegistrationException(String message) {
        super(message);
    }

    public NotifyListenerRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
