package org.openspaces.events.notify;

import com.gigaspaces.events.DataEventSession;
import com.gigaspaces.events.EventSessionConfig;
import com.gigaspaces.events.EventSessionFactory;
import com.gigaspaces.events.NotifyActionType;
import com.j_spaces.core.client.INotifyDelegatorFilter;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import org.openspaces.core.GigaSpaceException;
import org.openspaces.events.AbstractEventListenerContainer;
import org.openspaces.events.EventTemplateProvider;
import org.springframework.core.Constants;
import org.springframework.util.Assert;

import java.rmi.RemoteException;

/**
 * <p>The {@link #setTemplate(Object)} parameter is required in order to perform matching on which
 * events to receive. If the {@link #setEventListener(org.openspaces.events.SpaceDataEventListener)}
 * implements {@link org.openspaces.events.EventTemplateProvider} and the template is directly set,
 * the event listener will be used to get the template. This feature helps when event listeners
 * directly can only work with a certain template and removes the requirement of configuring the
 * template as well.
 *
 * @author kimchy
 */
public abstract class AbstractNotifyEventListenerContainer extends AbstractEventListenerContainer {

    private static final String COM_TYPE_PREFIX = "COM_TYPE_";

    /**
     */
    public static final int COM_TYPE_UNICAST = 0;

    /**
     */
    public static final int COM_TYPE_MULTIPLEX = 1;

    /**
     */
    public static final int COM_TYPE_MULTICAST = 2;


    private static final Constants constants = new Constants(AbstractNotifyEventListenerContainer.class);


    private int comType = COM_TYPE_UNICAST;

    private boolean fifo = false;

    private Integer batchSize;

    private Integer batchTime;

    private boolean renew = false;

    private Object template;

    private long listenerLease = Lease.FOREVER;

    private INotifyDelegatorFilter notifyFilter;

    private boolean notifyWrite = false;

    private boolean notifyUpdate = false;

    private boolean notifyTake = false;

    private boolean notifyLeaseExpire = false;

    private boolean triggerNotifyTemplate = false;

    private boolean replicateNotifyTemplate = false;


    public void setComType(int comType) {
        this.comType = comType;
    }

    public void setComTypeName(String comTypeName) {
        Assert.notNull(comTypeName, "comTypeName cannot be null");
        setComType(constants.asNumber(COM_TYPE_PREFIX + comTypeName).intValue());
    }

    public void setFifo(boolean fifo) {
        this.fifo = fifo;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public void setBatchTime(Integer batchTime) {
        this.batchTime = batchTime;
    }

    public void setRenew(boolean renew) {
        this.renew = renew;
    }

    public void setTemplate(Object template) {
        this.template = template;
    }

    public void setListenerLease(long listenerLease) {
        this.listenerLease = listenerLease;
    }

    public void setNotifyFilter(INotifyDelegatorFilter notifyFilter) {
        this.notifyFilter = notifyFilter;
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

    public void setTriggerNotifyTemplate(boolean triggerNotifyTemplate) {
        this.triggerNotifyTemplate = triggerNotifyTemplate;
    }

    public void setReplicateNotifyTemplate(boolean replicateNotifyTemplate) {
        this.replicateNotifyTemplate = replicateNotifyTemplate;
    }


    public void initialize() throws GigaSpaceException {
        if (getEventListener() != null && getEventListener() instanceof EventTemplateProvider && template != null) {
            setTemplate(((EventTemplateProvider) getEventListener()).getTemplate());
        }

        super.initialize();
    }

    protected void validateConfiguration() {
        super.validateConfiguration();
        if (batchSize == null && batchTime != null) {
            throw new IllegalArgumentException("batchTime has value [" + batchTime + "] which enables batching. batchSize must have a value as well");
        }
        if (batchTime == null && batchSize != null) {
            throw new IllegalArgumentException("batchSize has value [" + batchSize + "] which enables batching. batchTime must have a value as well");
        }
        Assert.notNull(template, "template property is required");
    }

    protected EventSessionFactory createEventSessionFactory() {
        return EventSessionFactory.getFactory(getGigaSpace().getSpace());
    }

    protected EventSessionConfig createEventSessionConfig() throws IllegalArgumentException {
        EventSessionConfig eventSessionConfig = new EventSessionConfig();
        switch (comType) {
            case COM_TYPE_UNICAST:
                eventSessionConfig.setComType(EventSessionConfig.ComType.UNICAST);
                break;
            case COM_TYPE_MULTIPLEX:
                eventSessionConfig.setComType(EventSessionConfig.ComType.MULTIPLEX);
                break;
            case COM_TYPE_MULTICAST:
                eventSessionConfig.setComType(EventSessionConfig.ComType.MULTICAST);
                break;
            default:
                throw new IllegalArgumentException("Unknown com type [" + comType + "]");
        }
        eventSessionConfig.setFifo(fifo);
        if (batchSize != null && batchTime != null) {
            eventSessionConfig.setBatch(batchSize.intValue(), batchTime.intValue());
        }
        eventSessionConfig.setRenew(renew);
        eventSessionConfig.setTriggerNotifyTemplate(triggerNotifyTemplate);
        eventSessionConfig.setReplicateNotifyTemplate(replicateNotifyTemplate);
        return eventSessionConfig;
    }

    protected DataEventSession createDataEventSession(EventSessionFactory factory) throws GigaSpaceException {
        EventSessionConfig config = createEventSessionConfig();
        try {
            return factory.newDataEventSession(config, null);
        } catch (RemoteException e) {
            throw new CannotCreateNotifySessionException("Failed to create new data event session", config, e);
        }
    }

    protected void registerListener(DataEventSession dataEventSession, RemoteEventListener listener) throws NotifyListenerRegistrationException {
        NotifyActionType notifyType = NotifyActionType.NOTIFY_NONE;
        if (notifyWrite) {
            notifyType = notifyType.or(NotifyActionType.NOTIFY_WRITE);
        }
        if (notifyUpdate) {
            notifyType = notifyType.or(NotifyActionType.NOTIFY_UPDATE);
        }
        if (notifyTake) {
            notifyType = notifyType.or(NotifyActionType.NOTIFY_TAKE);
        }
        if (notifyLeaseExpire) {
            notifyType = notifyType.or(NotifyActionType.NOTIFY_LEASE_EXPIRATION);
        }
        try {
            dataEventSession.addListener(template, listener, listenerLease, null, notifyFilter, notifyType);
        } catch (Exception e) {
            throw new NotifyListenerRegistrationException("Failed to register notify listener", e);
        }
    }
}
