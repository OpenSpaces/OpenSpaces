package org.openspaces.events.notify;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * A failure to register a notify listener.
 * 
 * @author kimchy
 */
public class NotifyListenerRegistrationException extends DataAccessResourceFailureException {

    private static final long serialVersionUID = -8079394141467235611L;

    public NotifyListenerRegistrationException(String message) {
        super(message);
    }

    public NotifyListenerRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
