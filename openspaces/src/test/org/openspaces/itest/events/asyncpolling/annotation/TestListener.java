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
package org.openspaces.itest.events.asyncpolling.annotation;

import org.openspaces.events.EventTemplate;
import org.openspaces.events.TransactionalEvent;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.asyncpolling.AsyncHandler;
import org.openspaces.events.asyncpolling.AsyncPolling;
import org.openspaces.events.asyncpolling.receive.AsyncOperationHandler;
import org.openspaces.events.asyncpolling.receive.SingleTakeAsyncOperationHandler;
import org.openspaces.itest.core.simple.Message;

/**
 * @author kimchy
 */
@AsyncPolling
@TransactionalEvent
public class TestListener {

    private volatile boolean receivedMessage = false;

    @AsyncHandler
    public AsyncOperationHandler thisIsMyReceiveOperationHandler() {
        return new SingleTakeAsyncOperationHandler();
    }

    @EventTemplate
    public Message thisIsMyTemplate() {
        return new Message();
    }

    @SpaceDataEvent
    public void iAmTheListener(Object value) {
        receivedMessage = true;
    }

    public boolean isReceivedMessage() {
        return receivedMessage;
    }

    public void setReceivedMessage(boolean receivedMessage) {
        this.receivedMessage = receivedMessage;
    }
}
