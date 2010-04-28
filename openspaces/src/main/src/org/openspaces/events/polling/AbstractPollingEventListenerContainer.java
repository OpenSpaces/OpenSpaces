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

package org.openspaces.events.polling;

import org.openspaces.core.SpaceInterruptedException;
import org.openspaces.events.AbstractTransactionalEventListenerContainer;
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.events.polling.receive.ReceiveOperationHandler;
import org.openspaces.events.polling.receive.SingleTakeReceiveOperationHandler;
import org.openspaces.events.polling.trigger.TriggerOperationHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base class for listener container implementations which are based on polling. Provides support
 * for listener handling based on Space take operations.
 *
 * <p>This listener container variant is built for repeated polling attempts, each invoking the
 * {@link #receiveAndExecute} method. The receive timeout for each attempt can be configured through
 * the {@link #setReceiveTimeout "receiveTimeout"} property.
 *
 * <p>The container allows to set the template object used for the operations. Note, this can be a Pojo
 * based template, or one of GigaSpace's query classes such as
 * {@link com.j_spaces.core.client.SQLQuery}.
 *
 * <p>A pluggable receive operation handler can be provided by setting
 * {@link #setReceiveOperationHandler(org.openspaces.events.polling.receive.ReceiveOperationHandler)}.
 * The default handler used it
 * {@link org.openspaces.events.polling.receive.SingleTakeReceiveOperationHandler}.
 *
 * <p>Event reception and listener execution can automatically be wrapped in transactions through
 * passing a Spring {@link org.springframework.transaction.PlatformTransactionManager} into the
 * {@link #setTransactionManager transactionManager} property. This will usually be a
 * {@link org.openspaces.core.transaction.manager.LocalJiniTransactionManager LocalJiniTransactionManager}.
 *
 * <p>This base class does not assume any specific mechanism for asynchronous execution of polling
 * invokers. Check out {@link SimplePollingEventListenerContainer} for a concrete implementation
 * which is based on Spring's {@link org.springframework.core.task.TaskExecutor} abstraction,
 * including dynamic scaling of concurrent consumers and automatic self recovery.
 *
 * <p>The {@link #setTemplate(Object)} parameter is required in order to perform matching on which
 * events to receive. If the {@link #setEventListener(org.openspaces.events.SpaceDataEventListener)}
 * implements {@link org.openspaces.events.EventTemplateProvider} and the template is directly set,
 * the event listener will be used to get the template. This feature helps when event listeners
 * directly can only work with a certain template and removes the requirement of configuring the
 * template as well.
 *
 * <p>An advance feature allows for pluggable
 * {@link #setTriggerOperationHandler(org.openspaces.events.polling.trigger.TriggerOperationHandler) triggerOperationHandler}
 * which mainly makes sense when using transactions. The trigger operations handler allows to
 * perform a trigger receive outside of a transaction scope, and if it returned a value, perform the
 * take within a transaction. A useful implementation of it is
 * {@link org.openspaces.events.polling.trigger.ReadTriggerOperationHandler}.
 *
 * @author kimchy
 */
public abstract class AbstractPollingEventListenerContainer extends AbstractTransactionalEventListenerContainer {

    /**
     * The default receive timeout: 60000 ms = 60 seconds = 1 minute.
     */
    public static final long DEFAULT_RECEIVE_TIMEOUT = 60000;

    private boolean passArrayAsIs = false;

    private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

    private ReceiveOperationHandler receiveOperationHandler;

    private TriggerOperationHandler triggerOperationHandler;


    /**
     * If set to <code>true</code> will pass an array value returned from a
     * {@link org.openspaces.events.polling.receive.ReceiveOperationHandler}
     * directly to the listener without "serializing" it as one array element
     * each time. Defaults to <code>false</code>
     */
    public void setPassArrayAsIs(boolean passArrayAsIs) {
        this.passArrayAsIs = passArrayAsIs;
    }

    protected boolean isPassArrayAsIs() {
        return this.passArrayAsIs;
    }

    /**
     * Set the timeout to use for receive calls, in <b>milliseconds</b>. The default is 60000 ms,
     * that is, 1 minute.
     *
     * <p><b>NOTE:</b> This value needs to be smaller than the transaction timeout used by the
     * transaction manager (in the appropriate unit, of course).
     *
     * @see org.openspaces.core.GigaSpace#take(Object,long)
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    /**
     * Returns the timeout used for receive calls, in <b>millisecond</b>. The default is 60000 ms,
     * that is, 1 minute.
     */
    protected long getReceiveTimeout() {
        return receiveTimeout;
    }

    /**
     * Allows to set a receive operation handler that will perform the actual receive operation.
     * Defaults to {@link org.openspaces.events.polling.receive.SingleTakeReceiveOperationHandler}.
     */
    public void setReceiveOperationHandler(ReceiveOperationHandler receiveOperationHandler) {
        this.receiveOperationHandler = receiveOperationHandler;
    }

    protected ReceiveOperationHandler getReceiveOperationHandler() {
        return this.receiveOperationHandler;
    }

    /**
     * An advance feature allows for pluggable
     * {@link TriggerOperationHandler triggerOperationHandler} which mainly makes sense when using
     * transactions. The trigger operations handler allows to perform a trigger receive outside of a
     * transaction scope, and if it returned a value, perform the take within a transaction. A
     * useful implementation of it is
     * {@link org.openspaces.events.polling.trigger.ReadTriggerOperationHandler}. Defaults to
     * <code>null</code>.
     */
    public void setTriggerOperationHandler(TriggerOperationHandler triggerOperationHandler) {
        this.triggerOperationHandler = triggerOperationHandler;
    }

    protected TriggerOperationHandler getTriggerOperationHandler() {
        return this.triggerOperationHandler;
    }

    public void initialize() {
        if (receiveOperationHandler == null) {
            if (getActualEventListener() != null) {
                // try and find an annotated one
                final AtomicReference<Method> ref = new AtomicReference<Method>();
                ReflectionUtils.doWithMethods(AopUtils.getTargetClass(getActualEventListener()), new ReflectionUtils.MethodCallback() {
                    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                        if (method.isAnnotationPresent(ReceiveHandler.class)) {
                            ref.set(method);
                        }
                    }
                });
                if (ref.get() != null) {
                    ref.get().setAccessible(true);
                    try {
                        setReceiveOperationHandler((ReceiveOperationHandler) ref.get().invoke(getActualEventListener()));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Failed to set ReceiveOperationHandler from method [" + ref.get().getName() + "]", e);
                    }
                }
            }
            if (receiveOperationHandler == null) {
                receiveOperationHandler = new SingleTakeReceiveOperationHandler();
            }
        }

        if (triggerOperationHandler == null && getActualEventListener() != null) {
            final AtomicReference<Method> ref = new AtomicReference<Method>();
            ReflectionUtils.doWithMethods(AopUtils.getTargetClass(getActualEventListener()), new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                    if (method.isAnnotationPresent(TriggerHandler.class)) {
                        ref.set(method);
                    }
                }
            });
            if (ref.get() != null) {
                ref.get().setAccessible(true);
                try {
                    setTriggerOperationHandler((TriggerOperationHandler) ref.get().invoke(getActualEventListener()));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to set ReceiveOperationHandler from method [" + ref.get().getName() + "]", e);
                }
            }
        }

        // Proceed with superclass initialization.
        super.initialize();
    }

    /**
     * Execute the listener for a message received from the given consumer, wrapping the entire
     * operation in an external transaction if demanded.
     *
     * @see #doReceiveAndExecute
     */
    protected boolean receiveAndExecute(SpaceDataEventListener eventListener) throws Throwable, TransactionException {
        Object template = getReceiveTemplate();
        // if trigger is configure, work using trigger outside of a possible transaction
        if (triggerOperationHandler != null) {
            Object trigger;
            try {
                trigger = triggerOperationHandler.triggerReceive(getReceiveTemplate(), getGigaSpace(), receiveTimeout);
            } catch (SpaceInterruptedException e) {
                return false;
            }
            if (logger.isTraceEnabled()) {
                logger.trace(message("Trigger operation handler returned [" + trigger + "]"));
            }
            if (trigger == null) {
                return false;
            }
            // if we are going to use the trigger result as a template
            if (triggerOperationHandler.isUseTriggerAsTemplate()) {
                template = trigger;
            }
        }
        if (this.getTransactionManager() != null) {
            // Execute receive within transaction.
            TransactionStatus status = this.getTransactionManager().getTransaction(this.getTransactionDefinition());
            boolean messageReceived;
            try {
                messageReceived = doReceiveAndExecute(eventListener, template, status);
            } catch (RuntimeException ex) {
                rollbackOnException(status, ex);
                throw ex;
            } catch (Error err) {
                rollbackOnException(status, err);
                throw err;
            }
            // if no message is received, rollback the transaction (for better performance).
            if (!status.isCompleted()) {
                if (!messageReceived || status.isRollbackOnly()) {
                    this.getTransactionManager().rollback(status);
                } else {
                    this.getTransactionManager().commit(status);
                }
            }
            return messageReceived;
        } else {
            return doReceiveAndExecute(eventListener, template, null);
        }
    }

    protected boolean doReceiveAndExecute(SpaceDataEventListener eventListener, Object template, TransactionStatus status) {
        Object dataEvent = receiveEvent(template);
        if (dataEvent != null) {
            if (dataEvent instanceof Object[] && !passArrayAsIs) {
                Object[] dataEvents = (Object[]) dataEvent;
                for (Object dataEvent1 : dataEvents) {
                    if (logger.isTraceEnabled()) {
                        logger.trace(message("Received event [" + dataEvent + "]"));
                    }
                    eventReceived(dataEvent1);
                    try {
                        invokeListener(eventListener, dataEvent1, status, null);
                    } catch (Throwable ex) {
                        if (status != null) {
                            // in case of an exception, we rollback the transaction and return
                            // (since we rolled back)
                            if (logger.isTraceEnabled()) {
                                logger.trace(message("Rolling back transaction because of listener exception thrown: " + ex));
                            }
                            status.setRollbackOnly();
                            handleListenerException(ex);
                            return true;
                        } else {
                            // in case we do not work within a transaction, just handle the
                            // exception and continue
                            handleListenerException(ex);
                        }
                    }
                }
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace(message("Received event [" + dataEvent + "]"));
                }
                if (passArrayAsIs && !(dataEvent instanceof Object[])) {
                    Object dataEventArr = Array.newInstance(dataEvent.getClass(), 1);
                    Array.set(dataEventArr, 0, dataEvent);
                    dataEvent = dataEventArr;
                }
                eventReceived(dataEvent);
                try {
                    invokeListener(eventListener, dataEvent, status, null);
                } catch (Throwable ex) {
                    if (status != null) {
                        if (logger.isTraceEnabled()) {
                            logger.trace(message("Rolling back transaction because of listener exception thrown: " + ex));
                        }
                        status.setRollbackOnly();
                    }
                    handleListenerException(ex);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Perform a rollback, handling rollback exceptions properly.
     *
     * @param status object representing the transaction
     * @param ex     the thrown application exception or error
     */
    private void rollbackOnException(TransactionStatus status, Throwable ex) {
        logger.trace(message("Initiating transaction rollback on application exception"), ex);
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

    /**
     * Receive an event
     */
    protected Object receiveEvent(Object template) throws DataAccessException {
        try {
            return receiveOperationHandler.receive(template, getGigaSpace(), getReceiveTimeout());
        } catch (SpaceInterruptedException e) {
            // we got an interrupted exception, it means no receive operation so return null.
            return null;
        }
    }

    /**
     * Template method that gets called right when a new message has been received, before
     * attempting to process it. Allows subclasses to react to the event of an actual incoming
     * message, for example adapting their consumer count.
     */
    protected void eventReceived(Object event) {
    }

}
