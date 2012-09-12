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
