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

import com.gigaspaces.cluster.activeelection.SpaceInitializationIndicator;
import com.gigaspaces.events.DataEventSession;
import com.gigaspaces.events.EventSessionFactory;
import com.gigaspaces.events.batching.BatchRemoteEvent;
import com.gigaspaces.events.batching.BatchRemoteEventListener;
import com.j_spaces.core.client.EntryArrivedRemoteEvent;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.UnusableEntryException;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceMonitors;
import org.springframework.dao.DataAccessException;
import org.springframework.util.ClassUtils;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * A simple notification based container allowing to register a
 * {@link org.openspaces.events.SpaceDataEventListener} that will be triggered by notifications.
 * Uses {@link AbstractNotifyEventListenerContainer} for configuration of different notification
 * registration parameters and transactional semantics.
 *
 * <p>The container can automatically take the notified event data (using {@link GigaSpace#take(Object)})
 * if the {@link #setPerformTakeOnNotify(boolean)} is set to <code>true</code>. Defaults to
 * <code>false</code>. If the flag is set to <code>true</code>, {@link #setIgnoreEventOnNullTake(boolean)}
 * can control of the event will be propagated to the event listener if the take operation returned
 * null.
 *
 * @author kimchy
 */
public class SimpleNotifyEventListenerContainer extends AbstractNotifyEventListenerContainer {

    private boolean performTakeOnNotify = false;

    private boolean ignoreEventOnNullTake = false;

    private DataEventSession dataEventSession;

    public SimpleNotifyEventListenerContainer() {
        // we register for notifications even when the embedded space is backup
        setActiveWhenPrimary(false);
    }

    /**
     * If set to <code>true</code> will remove the event from the space using take operation.
     * Default is <code>false</code>.
     */
    public void setPerformTakeOnNotify(boolean performTakeOnNotify) {
        this.performTakeOnNotify = performTakeOnNotify;
    }

    /**
     * If set to <code>true</code>, will not propagate the event if the take operation returned
     * <code>null</code>. This flag only makes sense when
     * {@link #setPerformTakeOnNotify(boolean)} is set to <code>true</code>. Defaults to <code>false</code>.
     */
    public void setIgnoreEventOnNullTake(boolean ignoreEventOnNullTake) {
        this.ignoreEventOnNullTake = ignoreEventOnNullTake;
    }

    protected void doInitialize() throws DataAccessException {
    }

    protected void doShutdown() throws DataAccessException {
        closeSession();
    }

    protected void doAfterStart() throws DataAccessException {
        super.doAfterStart();
        registerListener();
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(getBeanName()).append("] ").append("Started");
            if (getTransactionManager() != null) {
                sb.append(" transactional");
            }
            sb.append(" notify event container");
            if (getTemplate() != null) {
                sb.append(", tempalte ").append(ClassUtils.getShortName(getTemplate().getClass())).append("[").append(getTemplate()).append("]");
            } else {
                sb.append(", tempalte [null]");
            }
            sb.append(", notifications [");
            if (getNotifyWrite() != null && getNotifyWrite()) {
                sb.append("write,");
            }
            if (getNotifyUpdate() != null && getNotifyUpdate()) {
                sb.append("update,");
            }
            if (getNotifyTake() != null && getNotifyTake()) {
                sb.append("take,");
            }
            if (getNotifyLeaseExpire() != null && getNotifyLeaseExpire()) {
                sb.append("leaseExpire,");
            }
            sb.append("]");
            logger.debug(sb.toString());
        }
    }

    protected void doBeforeStop() throws DataAccessException {
        super.doBeforeStop();
        closeSession();
        if (logger.isDebugEnabled()) {
            logger.debug("Stopped notify event container");
        }
    }

    protected void registerListener() throws DataAccessException {
        if (dataEventSession != null) {
            // we already registered the listener, just return.
            return;
        }
        SpaceInitializationIndicator.setInitializer();
        try {
            EventSessionFactory factory = createEventSessionFactory();
            dataEventSession = createDataEventSession(factory);
            try {
                if (isBatchEnabled()) {
                    registerListener(dataEventSession, new BatchNotifyListenerDelegate());
                } else {
                    registerListener(dataEventSession, new NotifyListenerDelegate());
                }
            } catch (NotifyListenerRegistrationException ex) {
                // in case of an exception, close the session
                closeSession();
                throw ex;
            }
        } finally {
            SpaceInitializationIndicator.unsetInitializer();
        }
    }

    protected void closeSession() {
        if (dataEventSession != null) {
            try {
                dataEventSession.close();
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(message("Failed to close data event session"), e);
                }
            } finally {
                dataEventSession = null;
            }
        }
    }

    public ServiceDetails[] getServicesDetails() {
        Object tempalte = getTemplate();
        if (!(tempalte instanceof Serializable)) {
            tempalte = null;
        }
        // for now, LRMI class loader problems
        tempalte = null;
        return new ServiceDetails[]{new NotifyEventContainerServiceDetails(beanName, getGigaSpace().getName(), tempalte, isPerformSnapshot(),
                getCommType(), isFifo(), getBatchSize(), getBatchTime(), isAutoRenew(),
                isNotifyAll(), isNotifyWrite(), isNotifyUpdate(), isNotifyWrite(), isNotifyLeaseExpire(), isNotifyUnmatched(),
                isTriggerNotifyTemplate(), isReplicateNotifyTemplate(), isPerformSnapshot(), isPassArrayAsIs())};
    }

    public ServiceMonitors[] getServicesMonitors() {
        return new ServiceMonitors[]{new NotifyEventContainerServiceMonitors(beanName, processedEvents.get(), failedEvents.get())};
    }

    /**
     * A simple remote listener delegate that delegates remote events to invocations of the
     * registered
     * {@link org.openspaces.events.SpaceDataEventListener#onEvent(Object,org.openspaces.core.GigaSpace,org.springframework.transaction.TransactionStatus,Object)} .
     *
     * <p>Calls
     * {@link org.openspaces.events.notify.AbstractNotifyEventListenerContainer#invokeListenerWithTransaction(Object,Object,boolean,boolean)}
     * for a possible listener execution within a transaction and passed the
     * {@link org.openspaces.events.notify.SimpleNotifyEventListenerContainer#setPerformTakeOnNotify(boolean)}
     * flag.
     */
    private class NotifyListenerDelegate implements RemoteEventListener {

        public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException {
//            if (!isRunning()) {
//                return;
//            }
            Object eventData;
            try {
                eventData = ((EntryArrivedRemoteEvent) remoteEvent).getObject();
            } catch (net.jini.core.entry.UnusableEntryException e) {
                throw new UnusableEntryException("Failute to get object from event [" + remoteEvent + "]", e);
            }
            if (logger.isTraceEnabled()) {
                logger.trace(message("Received event [" + eventData + "]"));
            }
            invokeListenerWithTransaction(eventData, remoteEvent, performTakeOnNotify, ignoreEventOnNullTake);
        }
    }

    private class BatchNotifyListenerDelegate implements BatchRemoteEventListener {

        public void notifyBatch(BatchRemoteEvent batchRemoteEvent) throws UnknownEventException, RemoteException {
//            if (!isRunning()) {
//                return;
//            }
            invokeListenerWithTransaction(batchRemoteEvent, performTakeOnNotify, ignoreEventOnNullTake);
        }

        public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException {
//            if (!isRunning()) {
//                return;
//            }
            Object eventData;
            try {
                eventData = ((EntryArrivedRemoteEvent) remoteEvent).getObject();
            } catch (net.jini.core.entry.UnusableEntryException e) {
                throw new UnusableEntryException("Failute to get object from event [" + remoteEvent + "]", e);
            }
            if (logger.isTraceEnabled()) {
                logger.trace(message("Received event [" + eventData + "]"));
            }
            invokeListenerWithTransaction(eventData, remoteEvent, performTakeOnNotify, ignoreEventOnNullTake);
        }
    }
}
