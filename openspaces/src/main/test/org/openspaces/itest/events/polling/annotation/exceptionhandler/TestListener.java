/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.itest.events.polling.annotation.exceptionhandler;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventExceptionHandler;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.ExceptionHandler;
import org.openspaces.events.ListenerExecutionFailedException;
import org.openspaces.events.TransactionalEvent;
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
