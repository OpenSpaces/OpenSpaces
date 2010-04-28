package org.openspaces.itest.events.polling.exceptionhandler;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventExceptionHandler;
import org.openspaces.events.ListenerExecutionFailedException;
import org.springframework.transaction.TransactionStatus;

/**
 * @author kimchy (shay.banon)
 */
public class TestExceptionHandler implements EventExceptionHandler {

    private volatile boolean success;

    private volatile int failureCount;

    public void reset() {
        success = false;
        failureCount = 0;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void onSuccess(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) throws RuntimeException {
        success = true;
    }

    public void onException(ListenerExecutionFailedException exception, Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) throws RuntimeException {
        failureCount++;
        txStatus.setRollbackOnly();
    }
}
