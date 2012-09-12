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

package org.openspaces.events;

import org.openspaces.events.adapter.EventListenerAdapter;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple based class for {@link SpaceDataEventListener} based containers. Allowing to register a
 * listener and provides several support methods like
 * {@link #invokeListener(SpaceDataEventListener, Object, org.springframework.transaction.TransactionStatus, Object)} in
 * order to simplify event listener based containers.
 *
 * @author kimchy
 */
public abstract class AbstractEventListenerContainer extends AbstractSpaceListeningContainer implements ApplicationContextAware {

    private SpaceDataEventListener eventListener;

    private String eventListenerRef;

    private ApplicationContext applicationContext;

    protected EventExceptionHandler exceptionHandler;

    protected final AtomicLong processedEvents = new AtomicLong();

    protected final AtomicLong failedEvents = new AtomicLong();

    protected EventExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * Sets an exception handler that will be invoked when an exception occurs on the listener allowing to
     * customize the handling of such cases.
     */
    public void setExceptionHandler(EventExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Sets the event listener implementation that will be used to delegate events to. Also see
     * different adapter classes provided for simpler event listeners integration.
     *
     * @param eventListener The event listener used
     */
    public void setEventListener(SpaceDataEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Sets an event listener bean reference name that will be used to lookup the actual listener
     * bean (based on its name). Mainly used when configuring a listener with specific scope setting
     * (such as prototype) allowing to scope to take affect by using <code>getBean</code> for each
     * request of the listener.
     */
    public void setEventListenerRef(String eventListenerRef) {
        this.eventListenerRef = eventListenerRef;
    }

    protected SpaceDataEventListener getEventListener() {
        if (eventListener != null) {
            return eventListener;
        }
        if (eventListenerRef == null) {
            return null;
        }
        return (SpaceDataEventListener) applicationContext.getBean(eventListenerRef);
    }

    protected Object getActualEventListener() {
        Object listener = getEventListener();
        while (listener instanceof EventListenerAdapter) {
            listener = ((EventListenerAdapter) listener).getActualEventListener();
        }
        return listener;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    protected Class getEventListenerClass() {
        if (eventListener != null) {
            return eventListener.getClass();
        }
        return applicationContext.getType(eventListenerRef);
    }

    @Override
    public void initialize() throws DataAccessException {
        if (exceptionHandler == null && getActualEventListener() != null) {
            final AtomicReference<Method> ref = new AtomicReference<Method>();
            ReflectionUtils.doWithMethods(AopUtils.getTargetClass(getActualEventListener()), new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                    if (method.isAnnotationPresent(ExceptionHandler.class)) {
                        ref.set(method);
                    }
                }
            });
            if (ref.get() != null) {
                ref.get().setAccessible(true);
                try {
                    setExceptionHandler((EventExceptionHandler) ref.get().invoke(getActualEventListener()));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to set EventExceptionHandler from method [" + ref.get().getName() + "]", e);
                }
            }

        }
        super.initialize();
    }

    /**
     * Only start if we have a listener registered. If we don't, then
     * explicit start should be called.
     */
    @Override
    protected void doStart() throws DataAccessException {
        if (getEventListener() != null) {
            super.doStart();
        }
    }

    // -------------------------------------------------------------------------
    // Template methods for listener execution
    // -------------------------------------------------------------------------

    /**
     * Executes the given listener if the container is running ({@link #isRunning()}.
     *
     * @param eventData The event data object
     * @param txStatus  An optional transaction status allowing to rollback a transaction programmatically
     * @param source    An optional source (or additional event information)
     */
    protected void executeListener(SpaceDataEventListener eventListener, Object eventData, TransactionStatus txStatus, Object source) throws Throwable {
        if (!isRunning()) {
            return;
        }
        invokeListener(eventListener, eventData, txStatus, source);
    }

    /**
     * Invokes the configured {@link org.openspaces.events.SpaceDataEventListener} based on the
     * provided data. Currently simply delegates to
     * {@link org.openspaces.events.SpaceDataEventListener#onEvent(Object,org.openspaces.core.GigaSpace,org.springframework.transaction.TransactionStatus,Object)}.
     *
     * @param eventData The event data object
     * @param txStatus  An optional transaction status allowing to rollback a transaction programmatically
     * @param source    An optional source (or additional event information)
     * @throws DataAccessException
     */
    protected void invokeListener(SpaceDataEventListener eventListener, Object eventData, TransactionStatus txStatus, Object source) throws Throwable {
        if (exceptionHandler != null) {
            try {
                eventListener.onEvent(eventData, getGigaSpace(), txStatus, source);
                exceptionHandler.onSuccess(eventData, getGigaSpace(), txStatus, source);
            } catch (Throwable e) {
                if (!(e instanceof ListenerExecutionFailedException)) {
                    e = new ListenerExecutionFailedException(e.getMessage(), e);
                }
                exceptionHandler.onException((ListenerExecutionFailedException) e, eventData, getGigaSpace(), txStatus, source);
            }
        } else {
            eventListener.onEvent(eventData, getGigaSpace(), txStatus, source);
        }
        processedEvents.incrementAndGet();
    }

    /**
     * Handles exception that occurs during the event listening process. Currently simply logs it.
     *
     * @param ex the exception to handle
     */
    protected void handleListenerException(Throwable ex) {
        if (ex instanceof Exception) {
            invokeExceptionListener((Exception) ex);
        }
        if (isActive()) {
            failedEvents.incrementAndGet();
            // Regular case: failed while active. Log at error level.
            logger.error(message("Execution of event listener failed"), ex);
        } else {
            // Rare case: listener thread failed after container shutdown.
            // Log at debug level, to avoid spamming the shutdown log.
            logger.debug(message("Listener exception after container shutdown"), ex);
        }
    }

    /**
     * A callback to handle exception. Possible extension point for registered exception listeners.
     */
    protected void invokeExceptionListener(Exception e) {
    }

    public long getProcessedEvents() {
        return processedEvents.get();
    }

    public long getFailedEvents() {
        return failedEvents.get();
    }
}
