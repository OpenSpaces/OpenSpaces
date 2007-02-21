package org.openspaces.events.notify;

import com.gigaspaces.events.DataEventSession;
import com.gigaspaces.events.EventSessionFactory;
import com.j_spaces.core.client.EntryArrivedRemoteEvent;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import org.openspaces.core.GigaSpaceException;

import java.rmi.RemoteException;

/**
 * <p>A simple notification based container allowing to register a {@link org.openspaces.events.SpaceDataEventListener}
 * that will be triggered by notifications. Uses {@link org.openspaces.events.notify.AbstractNotifyEventListenerContainer}
 * for configuration of different notification registration parameters.
 *
 * <p>Allows to control using {@link #setRegisterOnStartup(boolean)} if the listener will be registered for
 * notification on startup or registration will be controlled by the {@link #doStart()} and {@link #doStop()}
 * callbacks (which by default are triggered based on the current space mode - <code>PRIMARY</code> or <code>BACKUP</code>).
 * Default is <code>false</code> which means registration will occur when the space moves into <code>PRIMARY</code>
 * mode (assuming that {@link #setActiveWhenPrimary(boolean)} is set to <code>true</code>, which is the default).
 *
 * @author kimchy
 */
public class SimpleNotifyEventListenerContainer extends AbstractNotifyEventListenerContainer {

    private boolean registerOnStartup = false;


    private DataEventSession dataEventSession;

    public void setRegisterOnStartup(boolean registerOnStartup) {
        this.registerOnStartup = registerOnStartup;
    }

    protected void doInitialize() throws GigaSpaceException {
        if (registerOnStartup) {
            registerListener();
        }
    }

    protected void doShutdown() throws GigaSpaceException {
        closeSession();
    }

    protected void doStart() throws GigaSpaceException {
        super.doStart();
        if (!registerOnStartup) {
            registerListener();
        }
    }

    protected void doStop() throws GigaSpaceException {
        if (!registerOnStartup) {
            closeSession();
        }
        super.doStop();
    }

    protected void registerListener() throws GigaSpaceException {
        if (dataEventSession != null) {
            // we already registered the listener, just return.
            return;
        }
        EventSessionFactory factory = createEventSessionFactory();
        dataEventSession = createDataEventSession(factory);
        try {
            registerListener(dataEventSession, new NotifyListenerDelegate());
        } catch (NotifyListenerRegistrationException ex) {
            // in case of an exception, close the session
            closeSession();
            throw ex;
        }
    }

    protected void closeSession() {
        if (dataEventSession != null) {
            try {
                dataEventSession.close();
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to close data event session", e);
                }
            } finally {
                dataEventSession = null;
            }
        }
    }

    /**
     * A simple remote listener delgate that delegates remote events to invocations of
     * the registered {@link org.openspaces.events.SpaceDataEventListener#onEvent(Object,org.openspaces.core.GigaSpace,Object)}.
     */
    private class NotifyListenerDelegate implements RemoteEventListener {

        public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException {
            Object eventData;
            try {
                eventData = ((EntryArrivedRemoteEvent) remoteEvent).getObject();
            } catch (UnusableEntryException e) {
                throw new GigaSpaceException("Unusable entry", e);
            }
            executeListener(eventData, remoteEvent);
        }
    }
}
