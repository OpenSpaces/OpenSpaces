package org.openspaces.itest.events.polling.annotation.exceptionhandler;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.*;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.springframework.transaction.TransactionStatus;

/**
 * @author kimchy
 */
@Polling
@TransactionalEvent
public class TestListener {

    private volatile boolean receivedMessage = false;

    private volatile boolean exceptionHandlerSuccess = false;
    private volatile boolean exceptionHandlerException = false;

    private volatile boolean throwException;

    private volatile ListenerExecutionFailedException exception;

    public void reset() {
        receivedMessage = false;
        exceptionHandlerSuccess = false;
        exceptionHandlerException = false;
        throwException = false;
        exception = null;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    @EventTemplate
    public Object thisIsMyTemplate() {
        return new Object();
    }

    @ExceptionHandler
    public EventExceptionHandler exceptionHandler() {
        return new EventExceptionHandler() {
            public void onSuccess(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) throws RuntimeException {
                exceptionHandlerSuccess = true;
            }

            public void onException(ListenerExecutionFailedException exception, Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) throws RuntimeException {
                exceptionHandlerException = true;
                TestListener.this.exception = exception;
            }
        };
    }

    @SpaceDataEvent
    public void iAmTheListener(Object value) {
        receivedMessage = true;
        if (throwException) {
            throw new RuntimeException("FAIL");
        }
    }

    public boolean isReceivedMessage() {
        return receivedMessage;
    }

    public boolean isExceptionHandlerSuccess() {
        return exceptionHandlerSuccess;
    }

    public boolean isExceptionHandlerException() {
        return exceptionHandlerException;
    }

    public boolean isThrowException() {
        return throwException;
    }

    public ListenerExecutionFailedException getException() {
        return exception;
    }
}