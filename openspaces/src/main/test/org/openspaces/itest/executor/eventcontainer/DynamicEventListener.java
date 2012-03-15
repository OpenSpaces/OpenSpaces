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
package org.openspaces.itest.executor.eventcontainer;

import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;

/**
 * @author kimchy
 */
@EventDriven
@Polling(value = "test", gigaSpace = "gigaSpace1")
public class DynamicEventListener {

    private volatile boolean receivedEvent = false;

    @EventTemplate
    public Object template() {
        return new Object();
    }

    @SpaceDataEvent
    public void onEvent() {
        receivedEvent = true;
    }

    public boolean isReceivedEvent() {
        return receivedEvent;
    }

    public void setReceivedEvent(boolean receivedEvent) {
        this.receivedEvent = receivedEvent;
    }
}
