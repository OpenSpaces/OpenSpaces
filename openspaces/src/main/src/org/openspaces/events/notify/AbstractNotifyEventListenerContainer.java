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
 * <p>Base class for notifications based containers allowing to register listener that will be
 * triggered by the space if notifications occur. Provides all the necessary parameters that are
 * used by {@link com.gigaspaces.events.EventSessionConfig} and support methods for creating
 * {@link com.gigaspaces.events.EventSessionFactory} and {@link com.gigaspaces.events.DataEventSession}
 * objects.
 *
 * <p>The container allows to set the template object used for the notify registration. Note, this
 * can be a Pojo based template, or one of GigaSpace's query classes such as {@link com.j_spaces.core.client.SQLQuery}.
 *
 * <p>Masking of which operations will cause notifications can be set using {@link #setNotifyWrite(boolean)},
 * {@link #setNotifyUpdate(boolean)}, {@link #setNotifyTake(boolean)} and {@link #setNotifyLeaseExpire(boolean)}.
 * Note, all this flags are set to <code>false</code> by default, so at least one of them should be set to
 * <code>true</code> in order to receive any notifications.
 *
 * <p>Batching of notifications can be turned on by setting both {@link #setBatchSize(Integer)} and
 * {@link #setBatchTime(Integer)}.
 *
 * <p>Fifo ordering of raised notification can be controlled by setting {@link #setFifo(boolean)} flag
 * to <code>true</code>. Note, for a full fifo based ordering the relevant entries in the space should
 * be configured to be fifo as well.
 *
 * <p>Listener registration across replicated spaces can be set using {@link #setReplicateNotifyTemplate(boolean)}
 * and {@link #setTriggerNotifyTemplate(boolean)}.
 *
 * <p>The communication protocol between the space "server" and the even listener client can be
 * configured using either {@link #setComType(int)} or {@link #setComTypeName(String)}. The available
 * options are {@link #COM_TYPE_UNICAST}, {@link #COM_TYPE_MULTIPLEX} and {@link #COM_TYPE_MULTICAST}.
 * If using {@link #setComType(int)} the integer constant value should be used. If using
 * {@link #setComTypeName(String)} the actual name of the com type can be used (<code>unicast</code>,
 * <code>multiplex</code> or <code>multicast</code>). The default communication type is
 * {@link #COM_TYPE_UNICAST}.
 *
 * <p>The {@link #setTemplate(Object)} parameter is required in order to perform matching on which
 * events to receive. If the {@link #setEventListener(org.openspaces.events.SpaceDataEventListener)}
 * implements {@link org.openspaces.events.EventTemplateProvider} and the template is directly set,
 * the event listener will be used to get the template. This feature helps when event listeners
 * directly can only work with a certain template and removes the requirement of configuring the
 * template as well.
 *
 * @author kimchy
 * @see com.gigaspaces.events.EventSessionConfig
 * @see com.gigaspaces.events.EventSessionFactory
 * @see com.gigaspaces.events.DataEventSession
 */
public abstract class AbstractNotifyEventListenerContainer extends AbstractEventListenerContainer {

    private static final String COM_TYPE_PREFIX = "COM_TYPE_";

    /**
     * Controls how notification are propogated from the space to the listener. Unicast propogation
     * uses TCP unicast communication which is usually best for small amount of registered clients.
     * This is the default communication type.
     */
    public static final int COM_TYPE_UNICAST = 0;

    /**
     * Controls how notification are propogated from the space to the listener. Same as unicast
     * ({@link #COM_TYPE_UNICAST}) in terms of communication protocol but uses a single client side
     * multiplexer which handles all the dispatching to the different notification listeners.
     */
    public static final int COM_TYPE_MULTIPLEX = 1;

    /**
     * Controls how notification are propogated from the space to the listener. Multicast propogation
     * uses UDP multicast communication which is usually best for large amount of registered clients.
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

    /**
     * Determines if events arrives in the same order they were triggered by the space "server". Note,
     * for a full fifo based ordering the relevant entries in the space should be configured to be
     * fifo as well.
     */
    public void setFifo(boolean fifo) {
        this.fifo = fifo;
    }

    /**
     * If set, turns batching event notifications where the server space accumaltes notifications to be
     * sent and then send them in batch. The batch size controls the number of notifications that will be
     * batched before they are sent. Note, if setting this property the {@link #setBatchTime(Integer)}
     * must be set as well.
     */
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * If set, turns batching event notifications where the server space accumaltes notifications to be
     * sent and then send them in batch. The batch time controls the elapsed time until the batch buffer
     * is cleared and sent. The time is in <b>milliseconds</b>. Note, if setting this property the
     * {@link #setBatchSize(Integer)} must be set as well.
     */
    public void setBatchTime(Integer batchTime) {
        this.batchTime = batchTime;
    }

    /**
     * If {@link #setListenerLease(long)} is set, automatically performs lease renewal. Defaults to
     * <code>false</code>.
     *
     * @see #setListenerLease(long)
     */
    public void setRenew(boolean renew) {
        this.renew = renew;
    }

    /**
     * Sets the specified template to be used for notifications.
     */
    public void setTemplate(Object template) {
        this.template = template;
    }

    /**
     * Controls the lease associated with the registered listener. Defaults to {@link net.jini.core.lease.Lease#FOREVER}.
     *
     * @see #setRenew(boolean)
     */
    public void setListenerLease(long listenerLease) {
        this.listenerLease = listenerLease;
    }

    /**
     * Allows to register a filter on the server side that can filter out or modify notifications that
     * will be sent by the space "server". Note, this filter will be passed to the space server and used
     * there.
     */
    public void setNotifyFilter(INotifyDelegatorFilter notifyFilter) {
        this.notifyFilter = notifyFilter;
    }

    /**
     * Turns on notification for write operations. Defaults to <code>false</code>.
     */
    public void setNotifyWrite(boolean notifyWrite) {
        this.notifyWrite = notifyWrite;
    }

    /**
     * Turns on notification for update operations. Defaults to <code>false</code>.
     */
    public void setNotifyUpdate(boolean notifyUpdate) {
        this.notifyUpdate = notifyUpdate;
    }

    /**
     * Turns on notification for take operations. Defaults to <code>false</code>.
     */
    public void setNotifyTake(boolean notifyTake) {
        this.notifyTake = notifyTake;
    }

    /**
     * Turns on notification for least expiration. Defaults to <code>false</code>.
     */
    public void setNotifyLeaseExpire(boolean notifyLeaseExpire) {
        this.notifyLeaseExpire = notifyLeaseExpire;
    }

    /**
     * If using a replicated space controls if the listener that are replicated to cluster
     * members will raise notifications.
     *
     * @see #setReplicateNotifyTemplate(boolean)
     */
    public void setTriggerNotifyTemplate(boolean triggerNotifyTemplate) {
        this.triggerNotifyTemplate = triggerNotifyTemplate;
    }

    /**
     * If using a replicated space controls if the listener will be replicated between all the
     * replicated cluster members.
     *
     * @see #setTriggerNotifyTemplate(boolean)
     */
    public void setReplicateNotifyTemplate(boolean replicateNotifyTemplate) {
        this.replicateNotifyTemplate = replicateNotifyTemplate;
    }


    public void initialize() throws GigaSpaceException {
        if (getEventListener() != null && getEventListener() instanceof EventTemplateProvider && template != null) {
            setTemplate(((EventTemplateProvider) getEventListener()).getTemplate());
        }

        if (!replicateNotifyTemplate && triggerNotifyTemplate) {
            if (logger.isDebugEnabled()) {
                logger.debug("triggerNotifyTemplate is set, automatically setting replicateNotifyTemplate to true");
            }
            replicateNotifyTemplate = true;
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

    /**
     * Creates a new event session factory based on the space provided.
     */
    protected EventSessionFactory createEventSessionFactory() {
        return EventSessionFactory.getFactory(getGigaSpace().getSpace());
    }

    /**
     * Creates a new {@link com.gigaspaces.events.EventSessionConfig} based on the different
     * parameters this container accepts.
     */
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

    /**
     * Creates a new {@link com.gigaspaces.events.DataEventSession} based on the provided factory. Uses
     * {@link #createEventSessionConfig()} in order to create the session configuration.
     */
    protected DataEventSession createDataEventSession(EventSessionFactory factory) throws GigaSpaceException {
        EventSessionConfig config = createEventSessionConfig();
        try {
            return factory.newDataEventSession(config, null);
        } catch (RemoteException e) {
            throw new CannotCreateNotifySessionException("Failed to create new data event session", config, e);
        }
    }

    /**
     * Registers a listener using the provided {@link com.gigaspaces.events.DataEventSession} and based on
     * different parameters set on this container.
     */
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
