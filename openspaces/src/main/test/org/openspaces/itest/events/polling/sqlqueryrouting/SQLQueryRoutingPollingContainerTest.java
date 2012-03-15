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

import org.openspaces.core.GigaSpace;
import org.openspaces.events.polling.SimplePollingContainerConfigurer;
import org.openspaces.events.polling.SimplePollingEventListenerContainer;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

/**
 * Test SQLQuery.setRouting with two polling containers (1 for each partition).
 * 
 * @author Idan Moyal
 * @since 8.0.1
 *
 */
public class SQLQueryRoutingPollingContainerTest extends AbstractDependencyInjectionSpringContextTests {
    
    protected GigaSpace gigaSpace;
    
    public SQLQueryRoutingPollingContainerTest() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/events/polling/sqlqueryrouting/sqlquery-routing.xml"};
    }
    
    public void testSqlQueryRouting() throws Exception {
        PollingContainerListener listener1 = new PollingContainerListener(Integer.valueOf(1));
        PollingContainerListener listener2 = new PollingContainerListener(Integer.valueOf(2));
        SimplePollingEventListenerContainer container1 = new SimplePollingContainerConfigurer(gigaSpace).eventListenerAnnotation(listener1).pollingContainer();
        container1.start();
        SimplePollingEventListenerContainer container2 = new SimplePollingContainerConfigurer(gigaSpace).eventListenerAnnotation(listener2).pollingContainer();
        container2.start();
        
        gigaSpace.write(new MyClass(false, 1));
        gigaSpace.write(new MyClass(false, 2));
        
        Thread.sleep(500);
        
        assertEquals(1, listener1.getCount());
        assertEquals(1, listener2.getCount());
    }
    
    
    public static class MyClass {
        private String id;
        private Boolean processed;
        private Integer routing;
        
        public MyClass() {
        }
        public MyClass(Boolean processed, Integer routing) {
            this.processed = processed;
            this.setRouting(routing);
        }
        @SpaceId(autoGenerate = true)
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public Boolean getProcessed() {
            return processed;
        }
        public void setProcessed(Boolean processed) {
            this.processed = processed;
        }
        
        @Override
        public String toString() {
            return "MyClass [id=" + id + ", processed=" + processed + "]";
        }
        public void setRouting(Integer routing) {
            this.routing = routing;
        }
        @SpaceRouting
        public Integer getRouting() {
            return routing;
        }
        
    }
    
    
    
}
