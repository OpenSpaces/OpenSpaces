package org.openspaces.events;

import org.openspaces.core.GigaSpaceException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;

/**
 * @author kimchy
 */
public abstract class AbstractPollingEventListenerContainer extends AbstractEventListenerContainer {

    /**
     * The default receive timeout: 1000 ms = 1 second.
     */
    public static final long DEFAULT_RECEIVE_TIMEOUT = 1000;

    private Object template;

    private PlatformTransactionManager transactionManager;

    private DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();

    private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;


    /**
     * Sets the specified template to be used with the polling space operation.
     *
     * @see org.openspaces.core.GigaSpace#take(Object,long)
     */
    public void setTemplate(Object template) {
        this.template = template;
    }

    protected Object getTemplate() {
        return template;
    }

    /**
     * <p>Specify the Spring {@link org.springframework.transaction.PlatformTransactionManager}
     * to use for transactional wrapping of event reception plus listener execution.
     *
     * <p>Default is none, not performing any transactional wrapping.
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Return the Spring PlatformTransactionManager to use for transactional
     * wrapping of message reception plus listener execution.
     */
    protected final PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    /**
     * Specify the transaction name to use for transactional wrapping.
     * Default is the bean name of this listener container, if any.
     *
     * @see org.springframework.transaction.TransactionDefinition#getName()
     */
    public void setTransactionName(String transactionName) {
        this.transactionDefinition.setName(transactionName);
    }

    /**
     * Specify the transaction timeout to use for transactional wrapping, in <b>seconds</b>.
     * Default is none, using the transaction manager's default timeout.
     *
     * @see org.springframework.transaction.TransactionDefinition#getTimeout()
     * @see #setReceiveTimeout
     */
    public void setTransactionTimeout(int transactionTimeout) {
        this.transactionDefinition.setTimeout(transactionTimeout);
    }

    /**
     * <p>Set the timeout to use for receive calls, in <b>milliseconds</b>.
     * The default is 1000 ms, that is, 1 second.
     *
     * <p><b>NOTE:</b> This value needs to be smaller than the transaction
     * timeout used by the transaction manager (in the appropriate unit,
     * of course).
     *
     * @see org.openspaces.core.GigaSpace#take(Object,long)
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public void initialize() {
        // Use bean name as default transaction name.
        if (this.transactionDefinition.getName() == null) {
            this.transactionDefinition.setName(getBeanName());
        }

        // Proceed with superclass initialization.
        super.initialize();
    }


    protected void validateConfiguration() {
        super.validateConfiguration();
        Assert.isTrue(receiveTimeout >= 0, "receiveTimeout must have a non negative value");
        Assert.notNull(template, "template property is required");
    }

    /**
     * Execute the listener for a message received from the given consumer,
     * wrapping the entire operation in an external transaction if demanded.
     *
     * @see #doReceiveAndExecute
     */
    protected boolean receiveAndExecute() throws GigaSpaceException {
        if (this.transactionManager != null) {
            // Execute receive within transaction.
            TransactionStatus status = this.transactionManager.getTransaction(this.transactionDefinition);
            boolean messageReceived;
            try {
                messageReceived = doReceiveAndExecute(status);
            } catch (RuntimeException ex) {
                rollbackOnException(status, ex);
                throw ex;
            } catch (Error err) {
                rollbackOnException(status, err);
                throw err;
            }
            this.transactionManager.commit(status);
            return messageReceived;
        } else {
            // Execute receive outside of transaction.
            return doReceiveAndExecute(null);
        }
    }

    protected boolean doReceiveAndExecute(TransactionStatus status) {
        Object dataEvent = receiveEvent();
        if (dataEvent != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received event [" + dataEvent + "]");
            }
            eventReceived(dataEvent);
            try {
                invokeListener(dataEvent, null);
            }
            catch (Throwable ex) {
                if (status != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Rolling back transaction because of listener exception thrown: " + ex);
                    }
                    status.setRollbackOnly();
                }
                handleListenerException(ex);
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
        logger.debug("Initiating transaction rollback on application exception", ex);
        try {
            this.transactionManager.rollback(status);
        }
        catch (RuntimeException ex2) {
            logger.error("Application exception overridden by rollback exception", ex);
            throw ex2;
        }
        catch (Error err) {
            logger.error("Application exception overridden by rollback error", ex);
            throw err;
        }
    }

    /**
     * Receive an event
     */
    protected Object receiveEvent() throws GigaSpaceException {
        return getGigaSpace().take(getTemplate(), receiveTimeout);
    }

    /**
     * Template method that gets called right when a new message has been received,
     * before attempting to process it. Allows subclasses to react to the event
     * of an actual incoming message, for example adapting their consumer count.
     */
    protected void eventReceived(Object event) {
    }

}
