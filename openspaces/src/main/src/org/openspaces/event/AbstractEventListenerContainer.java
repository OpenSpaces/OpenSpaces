package org.openspaces.event;

import org.openspaces.core.GigaSpaceException;
import org.springframework.util.Assert;

/**
 * @author kimchy
 */
public abstract class AbstractEventListenerContainer extends AbstractSpaceListeningContainer {

    private SpaceDataEventListener eventListener;

    public void setEventListener(SpaceDataEventListener eventListener) {
        this.eventListener = eventListener;
    }

    protected SpaceDataEventListener getEventListener() {
        return eventListener;
    }

    protected void validateConfiguration() {
        super.validateConfiguration();
        Assert.notNull(eventListener, "eventListener must be specified");
    }

    //-------------------------------------------------------------------------
    // Template methods for listener execution
    //-------------------------------------------------------------------------

    protected void invokeListener(Object eventData, Object source) throws GigaSpaceException {
        eventListener.onEvent(eventData, getGigaSpace(), source);
    }

    /**
     * Handles exception that occurs during the event listening process. Currently simply
     * logs it.
     *
     * @param ex the exception to handle
     */
    protected void handleListenerException(Throwable ex) {
        if (ex instanceof Exception) {
            invokeExceptionListener((Exception) ex);
        }
        if (isActive()) {
            // Regular case: failed while active. Log at error level.
            logger.error("Execution of JMS message listener failed", ex);
        } else {
            // Rare case: listener thread failed after container shutdown.
            // Log at debug level, to avoid spamming the shutdown log.
            logger.debug("Listener exception after container shutdown", ex);
        }
    }

    /**
     * A callback to handle exception. Possible extension point for registered exception
     * listeners.
     */
    protected void invokeExceptionListener(Exception e) {
    }
}
