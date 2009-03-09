package org.openspaces.events;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.openspaces.core.transaction.manager.JiniPlatformTransactionManager;

/**
 * @author kimchy
 */
public abstract class AbstractTransactionalEventListenerContainer extends AbstractTemplateEventListenerContainer {

    private PlatformTransactionManager transactionManager;

    private DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();

    private boolean disableTransactionValidation = false;


    /**
     * Specify the Spring {@link org.springframework.transaction.PlatformTransactionManager} to use
     * for transactional wrapping of listener execution.
     *
     * <p>
     * Default is none, not performing any transactional wrapping.
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Return the Spring PlatformTransactionManager to use for transactional wrapping of message
     * reception plus listener execution.
     */
    protected final PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    /**
     * Specify the transaction name to use for transactional wrapping. Default is the bean name of
     * this listener container, if any.
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
     */
    public void setTransactionTimeout(int transactionTimeout) {
        this.transactionDefinition.setTimeout(transactionTimeout);
    }

    /**
     * Specify the transaction isolation to use for transactional wrapping.
     *
     * @see org.springframework.transaction.support.DefaultTransactionDefinition#setIsolationLevel(int)
     */
    public void setTransactionIsolationLevel(int transactionIsolationLevel) {
        this.transactionDefinition.setIsolationLevel(transactionIsolationLevel);
    }

    /**
     * Specify the transaction isolation to use for transactional wrapping.
     *
     * @see org.springframework.transaction.support.DefaultTransactionDefinition#setIsolationLevelName(String)
     */
    public void setTransactionIsolationLevelName(String transactionIsolationLevelName) {
        this.transactionDefinition.setIsolationLevelName(transactionIsolationLevelName);
    }

    protected DefaultTransactionDefinition getTransactionDefinition() {
        return transactionDefinition;
    }

    /**
     * Should transaction validation be enabled or not (verify and fail if transaction manager is
     * provided and the GigaSpace is not transactional). Default to <code>false</code>.
     */
    public void setDisableTransactionValidation(boolean disableTransactionValidation) {
        this.disableTransactionValidation = disableTransactionValidation;
    }

    @Override
    public void initialize() throws DataAccessException {
        // Use bean name as default transaction name.
        if (this.transactionDefinition.getName() == null) {
            this.transactionDefinition.setName(getBeanName());
        }
        super.initialize();
    }

    @Override
    protected void validateConfiguration() {
        super.validateConfiguration();
        if (transactionManager != null && !disableTransactionValidation) {
            if (!getGigaSpace().getTxProvider().isEnabled()) {
                throw new IllegalStateException(message("event container is configured to run under transactions (transaction manager is provided) " +
                        "but GigaSpace is not transactional. Please pass the transaction manager to the GigaSpace bean as well"));
            }
        }
    }

    public String getTransactionManagerName() {
        if (transactionManager instanceof JiniPlatformTransactionManager) {
            return ((JiniPlatformTransactionManager) transactionManager).getBeanName();
        }
        if (transactionManager != null) {
            return "<<unknown>>";
        }
        return null;
    }

    protected boolean isTransactional() {
        return transactionManager != null;
    }
}
