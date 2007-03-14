package org.openspaces.events.notify;

import org.springframework.dao.DataAccessResourceFailureException;

import com.gigaspaces.events.EventSessionConfig;

/**
 * A failure to create a notify session.
 * 
 * @author kimchy
 */
public class CannotCreateNotifySessionException extends DataAccessResourceFailureException {

    private static final long serialVersionUID = 8957193715747405306L;

    private EventSessionConfig config;

    public CannotCreateNotifySessionException(String message, EventSessionConfig config) {
        super(message);
        this.config = config;
    }

    public CannotCreateNotifySessionException(String message, EventSessionConfig config, Throwable cause) {
        super(message, cause);
        this.config = config;
    }

    public EventSessionConfig getConfig() {
        return config;
    }
}
