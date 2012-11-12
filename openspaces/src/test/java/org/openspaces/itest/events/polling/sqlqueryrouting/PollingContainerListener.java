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
package org.openspaces.itest.events.polling.sqlqueryrouting;

import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.openspaces.itest.events.pojos.MockPojo;

import com.j_spaces.core.client.SQLQuery;

/**
 * 
 * @author Idan Moyal
 * @since 8.0.1
 *
 */
@Polling
public class PollingContainerListener {

    private volatile int counter = 0;
    private Object routing = null;
    
    public PollingContainerListener() {
    }
    
    public PollingContainerListener(Object routing) {
        this.routing = routing;
    }
    
    @EventTemplate
    public Object getTemplate() {
        SQLQuery<MockPojo> query = new SQLQuery<MockPojo>(MockPojo.class, "processed = false");
        query.setRouting(routing);
        return query;
    }
    
    public int getCount() {
        return counter;
    }
    
    @SpaceDataEvent
    public void event(MockPojo value) {
        value.setProcessed(true);
        System.out.println("PollingContainer #" + routing + ": " + value);
        counter++;
    }
    
}
