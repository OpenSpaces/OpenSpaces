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

package org.openspaces.events.asyncpolling;

import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResult;
import org.openspaces.core.transaction.internal.TransactionalAsyncFutureListener;
import org.openspaces.events.AbstractTransactionalEventListenerContainer;
import org.openspaces.events.asyncpolling.receive.AsyncOperationHandler;
import org.openspaces.events.asyncpolling.receive.SingleTakeAsyncOperationHandler;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author kimchy
 */
public class SimpleAsyncPollingEventListenerContainer extends AbstractTransactionalEventListenerContainer {

    /**
     * The default receive timeout: 60000 ms = 60 seconds = 1 minute.
     */
    public static final long DEFAULT_RECEIVE_TIMEOUT = 60000;


    private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

    private AsyncOperationHandler asyncOperationHandler;

    private int concurrentConsumers = 1;


    private AsyncFutureListener listener = new AsyncEventListener();

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

    public void setAsyncOperationHandler(AsyncOperationHandler asyncOperationHandler) {
        this.asyncOperationHandler = asyncOperationHandler;
    }

    public void setConcurrentConsumers(int concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
    }

    @Override
    public void initialize() throws DataAccessException {
        if (asyncOperationHandler == null) {
            if (getActualEventListener() != null) {
                // try and find an annotated one
                final AtomicReference<Method> ref = new AtomicReference<Method>();
                ReflectionUtils.doWithMethods(getActualEventListener().getClass(), new ReflectionUtils.MethodCallback() {
                    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                        if (method.isAnnotationPresent(AsyncHandler.class)) {
                            ref.set(method);
                        }
                    }
                });
                if (ref.get() != null) {
                    ref.get().setAccessible(true);
                    try {
                        setAsyncOperationHandler((AsyncOperationHandler) ref.get().invoke(getActualEventListener()));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Failed to set AsyncOperationHandler from method [" + ref.get().getName() + "]", e);
                    }
                }
            }
            if (asyncOperationHandler == null) {
                asyncOperationHandler = new SingleTakeAsyncOperationHandler();
            }
        }
        super.initialize();
        this.getTransactionDefinition().setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    protected void doInitialize() throws DataAccessException {

    }

    protected void doShutdown() throws DataAccessException {

    }

    protected void doAfterStart() throws DataAccessException {
        super.doAfterStart();
        for (int i = 0; i < concurrentConsumers; i++) {
            reschedule(listener);
        }
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(getBeanName()).append("] ").append("Started");
            if (getTransactionManager() != null) {
                sb.append(" transactional");
            }
            sb.append(" async polling event container");
            if (getTemplate() != null) {
                sb.append(", tempalte ").append(ClassUtils.getShortName(getTemplate().getClass())).append("[").append(getTemplate()).append("]");
            } else {
                sb.append(", tempalte [null]");
            }
            logger.debug(sb.toString());
        }
    }


    private void reschedule(AsyncFutureListener listener) {
        if (!isRunning()) {
            return;
        }

        if (logger.isTraceEnabled()) {
            logger.trace(message("Rescheduling async receive operation"));
        }
        System.out.println("reschedule listener " + listener);

        if (this.getTransactionManager() != null) {
            // Execute receive within transaction.
            TransactionStatus status = this.getTransactionManager().getTransaction(this.getTransactionDefinition());
            try {
                asyncOperationHandler.asyncReceive(getReceiveTemplate(), getGigaSpace(), receiveTimeout, listener);
            } catch (RuntimeException ex) {
                rollbackOnException(status, ex);
                throw ex;
            } catch (Error err) {
                rollbackOnException(status, err);
                throw err;
            }
            if (!status.isCompleted()) {
                this.getTransactionManager().commit(status);
            }
        } else {
            asyncOperationHandler.asyncReceive(getReceiveTemplate(), getGigaSpace(), receiveTimeout, listener);
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

    private class AsyncEventListener implements TransactionalAsyncFutureListener {

        public void onTransactionalResult(AsyncResult asyncResult, TransactionStatus txStatus) {
            System.out.println("AsyncResult: " + asyncResult.getResult());
            if (asyncResult.getException() != null) {
                asyncResult.getException().printStackTrace();
                if (logger.isWarnEnabled()) {
                    logger.warn(message("Async result operation internal exception"), asyncResult.getException());
                }
            } else {
                if (asyncResult.getResult() != null) {
                    try {
                        executeListener(getEventListener(), asyncResult.getResult(), null, asyncResult);
                    } catch (RuntimeException e) {
                        handleListenerException(e);
                        throw e;
                    }
                }
            }
        }

        public void onPostCommitTransaction(AsyncResult asyncResult) {
            reschedule(this);
        }

        public void onPostRollbackTransaction(AsyncResult asyncResult) {
            reschedule(this);
        }

        public void onResult(AsyncResult asyncResult) {
            // do nothing here...
        }
    }
}
