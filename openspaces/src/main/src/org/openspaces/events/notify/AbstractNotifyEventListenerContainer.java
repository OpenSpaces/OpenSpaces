/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.events.notify;

import com.gigaspaces.events.DataEventSession;
import com.gigaspaces.events.EventSessionConfig;
import com.gigaspaces.events.EventSessionFactory;
import com.gigaspaces.events.NotifyActionType;
import com.gigaspaces.events.batching.BatchRemoteEvent;
import com.j_spaces.core.client.EntryArrivedRemoteEvent;
import com.j_spaces.core.client.INotifyDelegatorFilter;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.lease.LeaseListener;
import org.openspaces.core.UnusableEntryException;
import org.openspaces.core.util.SpaceUtils;
import org.openspaces.events.AbstractTransactionalEventListenerContainer;
import org.springframework.core.Constants;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.Assert;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Base class for notifications based containers allowing to register listener that will be
 * triggered by the space if notifications occur. Provides all the necessary parameters that are
 * used by {@link com.gigaspaces.events.EventSessionConfig} and support methods for creating
 * {@link com.gigaspaces.events.EventSessionFactory} and
 * {@link com.gigaspaces.events.DataEventSession} objects.
 *
 * <p>The container allows to set the template object used for the notify registration. Note, this can
 * be a Pojo based template, or one of GigaSpace's query classes such as
 * {@link com.j_spaces.core.client.SQLQuery}.
 *
 * <p>Masking of which operations will cause notifications can be set using
 * {@link #setNotifyWrite(Boolean)}, {@link #setNotifyUpdate(Boolean)},
 * {@link #setNotifyTake(Boolean)} and {@link #setNotifyLeaseExpire(Boolean)}. Note, if no flag is
 * set, notifications will be send for <b>write</b> operations.
 *
 * <p>Batching of notifications can be turned on by setting both {@link #setBatchSize(Integer)} and
 * {@link #setBatchTime(Integer)}. When turning on batch notifications, the listener can choose whether
 * to receive the events as an array (by setting {@link #setPassArrayAsIs(boolean)} to <code>true</code>,
 * or to receive the notifications one by one (setting it to <code>false</code>). By default, the
 * {@link #setPassArrayAsIs(boolean)} is set to <code>false</code>.
 *
 * <p>Fifo ordering of raised notification can be controlled by setting {@link #setFifo(boolean)} flag
 * to <code>true</code>. Note, for a full fifo based ordering the relevant entries in the space
 * should be configured to be fifo as well.
 *
 * <p>Listener registration across replicated spaces can be set using
 * {@link #setReplicateNotifyTemplate(boolean)} and {@link #setTriggerNotifyTemplate(boolean)}.
 *
 * <p>The communication protocol between the space "server" and the even listener client can be
 * configured using either {@link #setComType(int)} or {@link #setComTypeName(String)}. The
 * available options are {@link #COM_TYPE_UNICAST}, {@link #COM_TYPE_MULTIPLEX} and
 * {@link #COM_TYPE_MULTICAST}. If using {@link #setComType(int)} the integer constant value should
 * be used. If using {@link #setComTypeName(String)} the actual name of the com type can be used (<code>unicast</code>,
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
 * <p>Provides {@link #invokeListenerWithTransaction(Object,Object,boolean,boolean)} allowing to execute the
 * listener within a transactional context. Also allows for the performTakeOnNotify to control if a
 * take operation will be performed against the space with the given event data in order to remove it
 * from the space.
 *
 * @author kimchy
 * @see com.gigaspaces.events.EventSessionConfig
 * @see com.gigaspaces.events.EventSessionFactory
 * @see com.gigaspaces.events.DataEventSession
 */
public abstract class AbstractNotifyEventListenerContainer extends AbstractTransactionalEventListenerContainer {

    public static final String COM_TYPE_PREFIX = "COM_TYPE_";

    /**
     * Controls how notification are propagated from the space to the listener. Unicast propagation
     * uses TCP unicast communication which is usually best for small amount of registered clients.
     * This is the default communication type.
     */
    public static final int COM_TYPE_UNICAST = 0;

    /**
     * Controls how notification are propagated from the space to the listener. Same as unicast ({@link #COM_TYPE_UNICAST})
     * in terms of communication protocol but uses a single client side multiplexer which handles
     * all the dispatching to the different notification listeners.
     */
    public static final int COM_TYPE_MULTIPLEX = 1;

    /**
     * Controls how notification are propagated from the space to the listener. Multicast
     * propagation uses UDP multicast communication which is usually best for large amount of
     * registered clients.
     */
    public static final int COM_TYPE_MULTICAST = 2;

    private static final Constants constants = new Constants(AbstractNotifyEventListenerContainer.class);

    private int comType = COM_TYPE_UNICAST;

    private boolean fifo = false;

    private Integer batchSize;

    private Integer batchTime;

    private boolean autoRenew = false;

    private LeaseListener leaseListener;

    private long listenerLease = Lease.FOREVER;

    private INotifyDelegatorFilter notifyFilter;

    private Boolean notifyWrite;

    private Boolean notifyUpdate;

    private Boolean notifyTake;

    private Boolean notifyLeaseExpire;

    private Boolean notifyUnmatched;

    private Boolean notifyAll;

    private Boolean triggerNotifyTemplate;

    private Boolean replicateNotifyTemplate;

    private Boolean guaranteed;

    private boolean passArrayAsIs = false;

    /**
     * See {@link #setComTypeName(String)}.
     *
     * @see #COM_TYPE_MULTICAST
     * @see #COM_TYPE_MULTIPLEX
     * @see #COM_TYPE_UNICAST
     */
    public void setComType(int comType) {
        this.comType = comType;
    }

    protected int getCommType() {
        return this.comType;
    }

    /**
     * Sets the communication protocol for the notification registration.
     *
     * @see #COM_TYPE_MULTICAST
     * @see #COM_TYPE_MULTIPLEX
     * @see #COM_TYPE_UNICAST
     */
    public void setComTypeName(String comTypeName) {
        Assert.notNull(comTypeName, "comTypeName cannot be null");
        setComType(constants.asNumber(COM_TYPE_PREFIX + comTypeName).intValue());
    }

    /**
     * Determines if events arrives in the same order they were triggered by the space "server".
     * Note, for a full fifo based ordering the relevant entries in the space should be configured
     * to be fifo as well.
     */
    public void setFifo(boolean fifo) {
        this.fifo = fifo;
    }

    protected boolean isFifo() {
        return this.fifo;
    }

    /**
     * If set, turns batching event notifications where the server space accumalates notifications to
     * be sent and then send them in batch. The batch size controls the number of notifications that
     * will be batched before they are sent. Note, if setting this property the
     * {@link #setBatchTime(Integer)} must be set as well.
     */
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    protected Integer getBatchSize() {
        return this.batchSize;
    }

    /**
     * If set, turns batching event notifications where the server space accumalates notifications to
     * be sent and then send them in batch. The batch time controls the elapsed time until the batch
     * buffer is cleared and sent. The time is in <b>milliseconds</b>. Note, if setting this
     * property the {@link #setBatchSize(Integer)} must be set as well.
     */
    public void setBatchTime(Integer batchTime) {
        this.batchTime = batchTime;
    }

    protected Integer getBatchTime() {
        return this.batchTime;
    }

    /**
     * If {@link #setListenerLease(long)} is set, automatically performs lease renewal. Defaults to
     * <code>false</code>.
     *
     * @see #setListenerLease(long)
     */
    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    protected boolean isAutoRenew() {
        return this.autoRenew;
    }

    /**
     * If {@link #setAutoRenew(boolean)} is set to <code>true</code> sets the lease listener for
     * it.
     */
    public void setLeaseListener(LeaseListener leaseListener) {
        this.leaseListener = leaseListener;
    }

    /**
     * Controls the lease associated with the registered listener. Defaults to
     * {@link net.jini.core.lease.Lease#FOREVER}.
     *
     * @see #setAutoRenew(boolean)
     */
    public void setListenerLease(long listenerLease) {
        this.listenerLease = listenerLease;
    }

    /**
     * Allows to register a filter on the server side that can filter out or modify notifications
     * that will be sent by the space "server". Note, this filter will be passed to the space server
     * and used there.
     */
    public void setNotifyFilter(INotifyDelegatorFilter notifyFilter) {
        this.notifyFilter = notifyFilter;
    }

    /**
     * Turns on notifications for write operations. Defaults to <code>false</code>.
     */
    public void setNotifyWrite(Boolean notifyWrite) {
        this.notifyWrite = notifyWrite;
    }

    protected Boolean isNotifyWrite() {
        if (notifyWrite == null) {
            return false;
        }
        return this.notifyWrite;
    }

    /**
     * Turns on notifications for update operations. Defaults to <code>false</code>.
     */
    public void setNotifyUpdate(Boolean notifyUpdate) {
        this.notifyUpdate = notifyUpdate;
    }

    protected Boolean isNotifyUpdate() {
        if (notifyUpdate == null) {
            return false;
        }
        return this.notifyUpdate;
    }

    /**
     * Turns on notifications for take operations. Defaults to <code>false</code>.
     */
    public void setNotifyTake(Boolean notifyTake) {
        this.notifyTake = notifyTake;
    }

    protected Boolean isNotifyTake() {
        if (notifyTake == null) {
            return false;
        }
        return this.notifyTake;
    }

    /**
     * Turns on notifications for all operations. This flag will override all the other notify flags
     * (if set). Defaults to <code>false</code>.
     */
    public void setNotifyAll(Boolean notifyAll) {
        this.notifyAll = notifyAll;
    }

    protected Boolean isNotifyAll() {
        if (notifyAll == null) {
            return false;
        }
        return this.notifyAll;
    }

    /**
     * Turns on notification for least expiration. Defaults to <code>false</code>.
     */
    public void setNotifyLeaseExpire(Boolean notifyLeaseExpire) {
        this.notifyLeaseExpire = notifyLeaseExpire;
    }

    protected Boolean isNotifyLeaseExpire() {
        if (notifyLeaseExpire == null) {
            return false;
        }
        return this.notifyLeaseExpire;
    }

    /**
     * Turns on notifications for unmatched templates (a template that matched an entry
     * but not it does not). Defaults to <code>false</code>.
     */
    public void setNotifyUnmatched(Boolean notifyUnmatched) {
        this.notifyUnmatched = notifyUnmatched;
    }

    protected Boolean isNotifyUnmatched() {
        if (notifyUnmatched == null) {
            return false;
        }
        return this.notifyUnmatched;
    }

    /**
     * If using a replicated space controls if the listener that are replicated to cluster members
     * will raise notifications.
     *
     * @see #setReplicateNotifyTemplate(boolean)
     */
    public void setTriggerNotifyTemplate(boolean triggerNotifyTemplate) {
        this.triggerNotifyTemplate = triggerNotifyTemplate;
    }

    protected Boolean isTriggerNotifyTemplate() {
        return this.triggerNotifyTemplate;
    }

    /**
     * If using a replicated space controls if the listener will be replicated between all the
     * replicated cluster members.
     *
     * <p>If working directly with a cluster memeber, the default value will be <code>false</code>.
     * Otherwise, the default value will be based on the cluster schema (which is true for clusters
     * with backups).
     *
     * @see #setTriggerNotifyTemplate(boolean)
     */
    public void setReplicateNotifyTemplate(boolean replicateNotifyTemplate) {
        this.replicateNotifyTemplate = replicateNotifyTemplate;
    }

    protected Boolean isReplicateNotifyTemplate() {
        return this.replicateNotifyTemplate;
    }

    /**
     * Controls if notifications will be guaraneteed (at least once) in case of failover.
     */
    public void setGuaranteed(Boolean guaranteed) {
        this.guaranteed = guaranteed;
    }

    protected Boolean isGuaranteed() {
        if (guaranteed == null) {
            return false;
        }
        return guaranteed;
    }


    /**
     * When batching is turned on, should the batch of events be passed as an <code>Object[]</code> to
     * the listener. Default to <code>false</code> which means it will be passed one event at a time.
     */
    public void setPassArrayAsIs(boolean passArrayAsIs) {
        this.passArrayAsIs = passArrayAsIs;
    }

    protected boolean isPassArrayAsIs() {
        return this.passArrayAsIs;
    }

    protected Boolean getNotifyWrite() {
        return notifyWrite;
    }

    protected Boolean getNotifyUpdate() {
        return notifyUpdate;
    }

    protected Boolean getNotifyTake() {
        return notifyTake;
    }

    protected Boolean getNotifyLeaseExpire() {
        return notifyLeaseExpire;
    }

    protected Boolean getNotifyUnmatched() {
        return notifyUnmatched;
    }

    /**
     * Returns <code>true</code> when batching is enabled.
     */
    protected boolean isBatchEnabled() {
        return batchSize != null && batchTime != null;
    }

    public void initialize() throws DataAccessException {
        if (SpaceUtils.isRemoteProtocol(getGigaSpace().getSpace())) {
            
        } else {
            // if we are using a Space that was started in embedded mode, no need to replicate notify template
            // by default

            if (replicateNotifyTemplate == null && !SpaceUtils.isRemoteProtocol(getGigaSpace().getSpace())) {
                if (logger.isTraceEnabled()) {
                    logger.trace(message("Setting replicateNotifyTemplate to false since working with an embedded Space"));
                }
                replicateNotifyTemplate = false;
            }
        }
        if (replicateNotifyTemplate == null && triggerNotifyTemplate != null && triggerNotifyTemplate) {
            if (logger.isTraceEnabled()) {
                logger.trace(message("triggerNotifyTemplate is set, automatically setting replicateNotifyTemplate to true"));
            }
            replicateNotifyTemplate = true;
        }

        if (getTemplate() instanceof NotifyTypeProvider) {
            NotifyTypeProvider notifyTypeProvider = (NotifyTypeProvider) getTemplate();
            if (notifyTypeProvider.isLeaseExpire() != null && notifyLeaseExpire == null) {
                notifyLeaseExpire = notifyTypeProvider.isLeaseExpire();
            }
            if (notifyTypeProvider.isTake() != null && notifyTake == null) {
                notifyTake = notifyTypeProvider.isTake();
            }
            if (notifyTypeProvider.isUpdate() != null && notifyUpdate == null) {
                notifyUpdate = notifyTypeProvider.isUpdate();
            }
            if (notifyTypeProvider.isWrite() != null && notifyWrite == null) {
                notifyWrite = notifyTypeProvider.isWrite();
            }
            if (notifyTypeProvider.isUnamtched() != null && notifyUnmatched == null) {
                notifyUnmatched = notifyTypeProvider.isUnamtched();
            }
        }

        if (notifyAll == null && notifyTake == null && notifyUpdate == null && notifyWrite == null
                && notifyLeaseExpire == null && notifyUnmatched == null) {
            notifyWrite = true;
            if (logger.isTraceEnabled()) {
                logger.trace(message("No notify flag is set, setting write notify to true by default"));
            }
        }

        super.initialize();
    }

    protected void validateConfiguration() {
        super.validateConfiguration();
        if (batchSize == null && batchTime != null) {
            throw new IllegalArgumentException("batchTime has value [" + batchTime
                    + "] which enables batching. batchSize must have a value as well");
        }
        if (batchTime == null && batchSize != null) {
            throw new IllegalArgumentException("batchSize has value [" + batchSize
                    + "] which enables batching. batchTime must have a value as well");
        }
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
            eventSessionConfig.setBatch(batchSize, batchTime);
        }
        eventSessionConfig.setAutoRenew(autoRenew, leaseListener);
        if (triggerNotifyTemplate != null) {
            eventSessionConfig.setTriggerNotifyTemplate(triggerNotifyTemplate);
        }
        if (replicateNotifyTemplate != null) {
            eventSessionConfig.setReplicateNotifyTemplate(replicateNotifyTemplate);
        }
        if (guaranteed != null) {
            eventSessionConfig.setGuaranteedNotifications(guaranteed);
        }
        return eventSessionConfig;
    }

    /**
     * Creates a new {@link com.gigaspaces.events.DataEventSession} based on the provided factory.
     * Uses {@link #createEventSessionConfig()} in order to create the session configuration.
     */
    protected DataEventSession createDataEventSession(EventSessionFactory factory) throws DataAccessException {
        EventSessionConfig config = createEventSessionConfig();
        try {
            return factory.newDataEventSession(config, null);
        } catch (RemoteException e) {
            throw new CannotCreateNotifySessionException("Failed to create new data event session", config, e);
        }
    }

    /**
     * Registers a listener using the provided {@link com.gigaspaces.events.DataEventSession} and
     * based on different parameters set on this container.
     */
    protected void registerListener(DataEventSession dataEventSession, RemoteEventListener listener)
            throws NotifyListenerRegistrationException {
        NotifyActionType notifyType = NotifyActionType.NOTIFY_NONE;
        if (notifyWrite != null && notifyWrite) {
            notifyType = notifyType.or(NotifyActionType.NOTIFY_WRITE);
        }
        if (notifyUpdate != null && notifyUpdate) {
            notifyType = notifyType.or(NotifyActionType.NOTIFY_UPDATE);
        }
        if (notifyTake != null && notifyTake) {
            notifyType = notifyType.or(NotifyActionType.NOTIFY_TAKE);
        }
        if (notifyLeaseExpire != null && notifyLeaseExpire) {
            notifyType = notifyType.or(NotifyActionType.NOTIFY_LEASE_EXPIRATION);
        }
        if (notifyUnmatched != null && notifyUnmatched) {
            notifyType = notifyType.or(NotifyActionType.NOTIFY_UNMATCHED);
        }
        if (notifyAll != null && notifyAll) {
            notifyType = notifyType.or(NotifyActionType.NOTIFY_ALL);
        }
        try {
            dataEventSession.addListener(getReceiveTemplate(), listener, listenerLease, null, notifyFilter, notifyType);
        } catch (Exception e) {
            throw new NotifyListenerRegistrationException("Failed to register notify listener", e);
        }
    }

    protected void invokeListenerWithTransaction(BatchRemoteEvent batchRemoteEvent, boolean performTakeOnNotify,
                                                 boolean ignoreEventOnNullTake) throws DataAccessException {

        boolean invokeListener = true;
        TransactionStatus status = null;
        if (this.getTransactionManager() != null) {
            // Execute receive within transaction.
            status = this.getTransactionManager().getTransaction(this.getTransactionDefinition());
        }
        if (passArrayAsIs) {
            RemoteEvent[] events = batchRemoteEvent.getEvents();
            Object[] eventData = new Object[events.length];
            try {
                for (int i = 0; i < events.length; i++) {
                    try {
                        eventData[i] = ((EntryArrivedRemoteEvent) events[i]).getObject();
                    } catch (net.jini.core.entry.UnusableEntryException e) {
                        throw new UnusableEntryException("Failute to get object from event [" + events[i] + "]", e);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace(message("Received event [" + eventData[i] + "]"));
                    }
                }
                if (performTakeOnNotify) {
                    if (ignoreEventOnNullTake) {
                        ArrayList<Object> tempEventData = new ArrayList<Object>(eventData.length);
                        for (Object data : eventData) {
                            Object takeVal = getGigaSpace().take(data, 0);
                            if (takeVal != null) {
                                tempEventData.add(data);
                            }
                        }
                        if (tempEventData.isEmpty()) {
                            invokeListener = false;
                        } else {
                            eventData = tempEventData.toArray(new Object[tempEventData.size()]);
                        }
                    } else {
                        for (Object data : eventData) {
                            getGigaSpace().take(data, 0);
                        }
                    }
                }
                try {
                    if (invokeListener) {
                        invokeListener(getEventListener(), eventData, status, batchRemoteEvent);
                    }
                } catch (Throwable t) {
                    if (logger.isTraceEnabled()) {
                        logger.trace(message("Rolling back transaction because of listener exception thrown: " + t));
                    }
                    if (status != null) {
                        status.setRollbackOnly();
                    }
                    handleListenerException(t);
                }
            } catch (RuntimeException ex) {
                if (status != null) {
                    rollbackOnException(status, ex);
                }
                throw ex;
            } catch (Error err) {
                if (status != null) {
                    rollbackOnException(status, err);
                }
                throw err;
            }
        } else {
            for (RemoteEvent remoteEvent : batchRemoteEvent.getEvents()) {
                Object eventData;
                try {
                    try {
                        eventData = ((EntryArrivedRemoteEvent) remoteEvent).getObject();
                    } catch (net.jini.core.entry.UnusableEntryException e) {
                        throw new UnusableEntryException("Failute to get object from event [" + remoteEvent + "]", e);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace(message("Received event [" + eventData + "]"));
                    }
                    if (performTakeOnNotify) {
                        Object takeVal = getGigaSpace().take(eventData, 0);
                        if (ignoreEventOnNullTake && takeVal == null) {
                            invokeListener = false;
                        }
                        if (logger.isTraceEnabled()) {
                            logger.trace("Performed take on notify, invoke listener is [" + invokeListener + "]");
                        }
                    }
                    try {
                        if (invokeListener) {
                            invokeListener(getEventListener(), eventData, status, remoteEvent);
                        }
                    } catch (Throwable t) {
                        if (logger.isTraceEnabled()) {
                            logger.trace(message("Rolling back transaction because of listener exception thrown: " + t));
                        }
                        if (status != null) {
                            status.setRollbackOnly();
                        }
                        handleListenerException(t);
                    }
                } catch (RuntimeException ex) {
                    if (status != null) {
                        rollbackOnException(status, ex);
                    }
                    throw ex;
                } catch (Error err) {
                    if (status != null) {
                        rollbackOnException(status, err);
                    }
                    throw err;
                }
            }
        }
        if (status != null) {
            if (!status.isCompleted()) {
                if (status.isRollbackOnly()) {
                    this.getTransactionManager().rollback(status);
                } else {
                    this.getTransactionManager().commit(status);
                }
            }
        }
    }

    /**
     * Executes the given listener. If a {@link #setTransactionManager(PlatformTransactionManager)}
     * is provided will perform the listener execution within a transaction, if not, the listener
     * execution is performed without a transaction.
     *
     * <p>
     * If the performTakeOnNotify flag is set to <code>true</code> will also perform take
     * operation with the given event data (i.e. remove it from the space).
     *
     * @param eventData           The event data object
     * @param source              The remote notify event
     * @param performTakeOnNotify A flag indicating whether to perform take operation with the given event data
     * @throws DataAccessException
     */
    protected void invokeListenerWithTransaction(Object eventData, Object source, boolean performTakeOnNotify,
                                                 boolean ignoreEventOnNullTake) throws DataAccessException {
        boolean invokeListener = true;
        if (this.getTransactionManager() != null) {
            // Execute receive within transaction.
            TransactionStatus status = this.getTransactionManager().getTransaction(this.getTransactionDefinition());
            try {
                if (performTakeOnNotify) {
                    Object takeVal = getGigaSpace().take(eventData, 0);
                    if (ignoreEventOnNullTake && takeVal == null) {
                        invokeListener = false;
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Performed take on notify, invoke listener is [" + invokeListener + "]");
                    }
                }
                try {
                    if (invokeListener) {
                        invokeListener(getEventListener(), eventData, status, source);
                    }
                } catch (Throwable t) {
                    if (logger.isTraceEnabled()) {
                        logger.trace(message("Rolling back transaction because of listener exception thrown: " + t));
                    }
                    status.setRollbackOnly();
                    handleListenerException(t);
                }
            } catch (RuntimeException ex) {
                rollbackOnException(status, ex);
                throw ex;
            } catch (Error err) {
                rollbackOnException(status, err);
                throw err;
            }
            if (!status.isCompleted()) {
                if (status.isRollbackOnly()) {
                    this.getTransactionManager().rollback(status);
                } else {
                    this.getTransactionManager().commit(status);
                }
            }
        } else {
            if (performTakeOnNotify) {
                Object takeVal = getGigaSpace().take(eventData, 0);
                if (ignoreEventOnNullTake && takeVal == null) {
                    invokeListener = false;
                }
            }
            try {
                if (invokeListener) {
                    invokeListener(getEventListener(), eventData, null, source);
                }
            } catch (Throwable t) {
                handleListenerException(t);
            }
        }
    }

    /**
     * Perform a rollback, handling rollback exceptions properly.
     *
     * @param status object representing the transaction
     * @param ex     the thrown application exception or error
     */
    private void rollbackOnException(TransactionStatus status, Throwable ex) {
        if (logger.isDebugEnabled()) {
            logger.debug(message("Initiating transaction rollback on application exception"), ex);
        }
        try {
            this.getTransactionManager().rollback(status);
        } catch (RuntimeException ex2) {
            logger.error(message("Application exception overridden by rollback exception"), ex);
            throw ex2;
        } catch (Error err) {
            logger.error(message("Application exception overridden by rollback error"), ex);
            throw err;
        }
    }

}
