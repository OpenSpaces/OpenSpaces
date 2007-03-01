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
 * for configuration of different notification registration parameters and transactional sematics.
 *
 * <p>Allows to control using {@link #setRegisterOnStartup(boolean)} if the listener will be registered for
 * notification on startup or registration will be controlled by the {@link #doStart()} and {@link #doStop()}
 * callbacks (which by default are triggered based on the current space mode - <code>PRIMARY</code> or <code>BACKUP</code>).
 * Default is <code>false</code> which means registration will occur when the space moves into <code>PRIMARY</code>
 * mode (assuming that {@link #setActiveWhenPrimary(boolean)} is set to <code>true</code>, which is the default).
 *
 * <p>Allows to control if when the notification occurs the event data will also be removed from the space using
 * a take operation. The flag controlling it is {@link #setPerformTakeOnNotify(boolean)}.
 *
 * @author kimchy
 */
public class SimpleNotifyEventListenerContainer extends AbstractNotifyEventListenerContainer {

    private boolean registerOnStartup = false;

    private boolean performTakeOnNotify = false;

    private DataEventSession dataEventSession;

    public void setRegisterOnStartup(boolean registerOnStartup) {
        this.registerOnStartup = registerOnStartup;
    }

    /**
     * If set to <code>true</code> will remove the event from the space using take operation.
     * Default is <code>false</code>.
     */
    public void setPerformTakeOnNotify(boolean performTakeOnNotify) {
        this.performTakeOnNotify = performTakeOnNotify;
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
     * <p>A simple remote listener delgate that delegates remote events to invocations of
     * the registered {@link org.openspaces.events.SpaceDataEventListener#onEvent(Object,org.openspaces.core.GigaSpace,org.springframework.transaction.TransactionStatus,Object)} .
     *
     * <p>Calls {@link org.openspaces.events.notify.AbstractNotifyEventListenerContainer#invokeListenerWithTransaction(Object,Object,boolean)}
     * for a possible listener execution within a transaction and passed the
     * {@link org.openspaces.events.notify.SimpleNotifyEventListenerContainer#setPerformTakeOnNotify(boolean)} flag.
     */
    private class NotifyListenerDelegate implements RemoteEventListener {

        public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException {
            if (registerOnStartup) {
                if (!isRunning()) {
                    return;
                }
            }
            Object eventData;
            try {
                eventData = ((EntryArrivedRemoteEvent) remoteEvent).getObject();
            } catch (UnusableEntryException e) {
                throw new GigaSpaceException("Unusable entry", e);
            }
            invokeListenerWithTransaction(eventData, remoteEvent, performTakeOnNotify);
        }
    }
}
