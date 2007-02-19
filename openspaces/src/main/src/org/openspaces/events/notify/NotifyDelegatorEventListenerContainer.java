package org.openspaces.events.notify;

import com.j_spaces.core.client.EntryArrivedRemoteEvent;
import com.j_spaces.core.client.NotifyDelegator;
import com.j_spaces.core.client.NotifyModifiers;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import org.openspaces.core.GigaSpaceException;
import org.openspaces.events.AbstractEventListenerContainer;
import org.springframework.util.Assert;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class NotifyDelegatorEventListenerContainer extends AbstractEventListenerContainer {

    private Object template;

    private boolean fifo;

    private long listenerLease = Lease.FOREVER;

    private boolean notifyWrite = false;

    private boolean notifyUpdate = false;

    private boolean notifyTake = false;

    private boolean notifyLeaseExpire = false;

    private boolean registerOnStartup = true;


    private NotifyDelegator notifyDelegator;

    public void setTemplate(Object template) {
        this.template = template;
    }

    public void setFifo(boolean fifo) {
        this.fifo = fifo;
    }

    public void setListenerLease(long listenerLease) {
        this.listenerLease = listenerLease;
    }

    public void setNotifyWrite(boolean notifyWrite) {
        this.notifyWrite = notifyWrite;
    }

    public void setNotifyUpdate(boolean notifyUpdate) {
        this.notifyUpdate = notifyUpdate;
    }

    public void setNotifyTake(boolean notifyTake) {
        this.notifyTake = notifyTake;
    }

    public void setNotifyLeaseExpire(boolean notifyLeaseExpire) {
        this.notifyLeaseExpire = notifyLeaseExpire;
    }

    public void setRegisterOnStartup(boolean registerOnStartup) {
        this.registerOnStartup = registerOnStartup;
    }


    protected void validateConfiguration() {
        super.validateConfiguration();
        Assert.notNull(template, "template property is required");
    }

    protected void doInitialize() throws GigaSpaceException {
        if (registerOnStartup) {
            registerListener();
        }
    }

    protected void doShutdown() throws GigaSpaceException {
        closeNotifyDelegator();
    }


    protected void doStart() throws GigaSpaceException {
        super.doStart();
        if (!registerOnStartup) {
            registerListener();
        }
    }


    protected void doStop() throws GigaSpaceException {
        if (!registerOnStartup) {
            closeNotifyDelegator();
        }
        super.doStop();
    }

    protected void registerListener() throws NotifyListenerRegistrationException {
        int notifyMask = NotifyModifiers.NOTIFY_NONE;
        if (notifyWrite) {
            notifyMask |= NotifyModifiers.NOTIFY_WRITE;
        }
        if (notifyUpdate) {
            notifyMask |= NotifyModifiers.NOTIFY_UPDATE;
        }
        if (notifyTake) {
            notifyMask |= NotifyModifiers.NOTIFY_TAKE;
        }
        if (notifyLeaseExpire) {
            notifyMask |= NotifyModifiers.NOTIFY_LEASE_EXPIRATION;
        }
        try {
            notifyDelegator = new NotifyDelegator(getGigaSpace().getSpace(), template, null,
                    new NotifyListenerDelegate(), listenerLease, null, fifo, notifyMask);
        } catch (Exception e) {
            throw new NotifyListenerRegistrationException("Failed to register notify listener", e);
        }
    }

    protected void closeNotifyDelegator() {
        if (notifyDelegator != null) {
            try {
                notifyDelegator.close();
            } finally {
                notifyDelegator = null;
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
