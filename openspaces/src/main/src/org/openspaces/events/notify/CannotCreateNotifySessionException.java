package org.openspaces.events.notify;

import com.gigaspaces.events.EventSessionConfig;
import org.openspaces.core.GigaSpaceException;

/**
 * @author kimchy
 */
public class CannotCreateNotifySessionException extends GigaSpaceException {

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
