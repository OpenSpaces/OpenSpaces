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
package org.openspaces.itest.events.notify.batch.namespace;

import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;

/**
 * Uses xml to define the following:
 * // @Notify
 * // @NotifyType(write = true)
 * // @NotifyBatch(size = 2, time = 100000, passArrayAsIs = true)
 * // @TransactionalEvent
 * 
 * @see org.openspaces.itest.events.notify.batch.NotifyContainerBatchTests
 * @author Moran Avigdor
 */
public class TestListener {

    private volatile boolean receivedMessage = false;

    private volatile Object event;

    @EventTemplate
    public Object thisIsMyTemplate() {
        return new Object();
    }

    @SpaceDataEvent
    public void iAmTheListener(Object value) {
        receivedMessage = true;
        this.event = value;
    }

    public boolean isReceivedMessage() {
        return receivedMessage;
    }

    public Object getEvent() {
        return event;
    }
}
