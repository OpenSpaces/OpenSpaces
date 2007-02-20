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
 * @author kimchy
 */
public class SimpleNotifyEventListenerContainer extends AbstractNotifyEventListenerContainer {

    private DataEventSession dataEventSession;

    private boolean registerOnStartup = false;

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
