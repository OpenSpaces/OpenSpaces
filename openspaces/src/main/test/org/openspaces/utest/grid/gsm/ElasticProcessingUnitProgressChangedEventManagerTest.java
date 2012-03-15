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
package org.openspaces.utest.grid.gsm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEvent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.events.DefaultElasticGridServiceAgentProvisioningProgressChangedEvent;
import org.openspaces.admin.internal.gsa.events.DefaultElasticGridServiceAgentProvisioningProgressChangedEventManager;
import org.openspaces.admin.internal.gsa.events.InternalElasticGridServiceAgentProvisioningProgressChangedEventManager;
import org.springframework.util.StringUtils;

public class ElasticProcessingUnitProgressChangedEventManagerTest {

    private static final int EVENTS_TIMEOUT_SECONDS = 2; //TODO: Increase timeout if sporadic failures occur
    private InternalAdmin admin = null;
    private InternalElasticGridServiceAgentProvisioningProgressChangedEventManager eventManager;

    @Before
    public void beforeMethod() {
        InternalAdmin admin = (InternalAdmin)(new AdminFactory().useDaemonThreads(true).createAdmin());
        eventManager = new DefaultElasticGridServiceAgentProvisioningProgressChangedEventManager(admin);
    }
    
    @After
    public void afterMethod() {
        if (admin != null) {
            admin.close();
        }
    }
    
    
    /**
     * Register to the events before they are fired. Remove event listener. Fire more events.
     */
    @Test
    public void testAddRemoveListener() throws InterruptedException {
        
        ElasticGridServiceAgentProvisioningProgressChangedEvent[] events = { 
                createEvent("pu1"),
                createEvent("pu2"),
                createEvent("pu3")
        };
        
        final List<String> receivedEvents = new ArrayList<String>();
        final CountDownLatch latch = new CountDownLatch(3);
        ElasticGridServiceAgentProvisioningProgressChangedEventListener listener = 
                new ElasticGridServiceAgentProvisioningProgressChangedEventListener() {
            
            @Override
            public void elasticGridServiceAgentProvisioningProgressChanged(
                    ElasticGridServiceAgentProvisioningProgressChangedEvent event) {
                receivedEvents.add(event.getProcessingUnitName());
                latch.countDown();
            }
        };
        eventManager.add(listener);
        for (ElasticGridServiceAgentProvisioningProgressChangedEvent event : events) {
            eventManager.elasticGridServiceAgentProvisioningProgressChanged(event);
        }
        Assert.assertTrue(latch.await(EVENTS_TIMEOUT_SECONDS,TimeUnit.SECONDS));
        eventManager.remove(listener);
        for (ElasticGridServiceAgentProvisioningProgressChangedEvent event : events) {
            eventManager.elasticGridServiceAgentProvisioningProgressChanged(event);
        }
        Thread.sleep(EVENTS_TIMEOUT_SECONDS*1000);
        Assert.assertEquals("pu1,pu2,pu3",StringUtils.collectionToCommaDelimitedString(receivedEvents));
    }

    private DefaultElasticGridServiceAgentProvisioningProgressChangedEvent createEvent(String processingUnitName) {
        return createEvent(false,false,processingUnitName);
    }
    
    private DefaultElasticGridServiceAgentProvisioningProgressChangedEvent createEvent(boolean isComplete, boolean isUndeploying, String processingUnitName) {
        DefaultElasticGridServiceAgentProvisioningProgressChangedEvent e1 = new DefaultElasticGridServiceAgentProvisioningProgressChangedEvent();
        e1.setProcessingUnitName(processingUnitName);
        e1.setComplete(isComplete);
        e1.setUndeploying(isUndeploying);
        return e1;
    }
    
    /**
     * Register to the events after the are fired, includeExisting = true
     */
    @Test
    public void testExistingEvents() throws InterruptedException {
        
        ElasticGridServiceAgentProvisioningProgressChangedEvent[] events = { 
                createEvent("pu1"),
                createEvent("pu2"),
                createEvent("pu3")
        };
        
        final List<String> receivedEvents = new ArrayList<String>();
        final CountDownLatch latch = new CountDownLatch(3);
        ElasticGridServiceAgentProvisioningProgressChangedEventListener listener = 
                new ElasticGridServiceAgentProvisioningProgressChangedEventListener() {
            
            @Override
            public void elasticGridServiceAgentProvisioningProgressChanged(
                    ElasticGridServiceAgentProvisioningProgressChangedEvent event) {
                receivedEvents.add(event.getProcessingUnitName());
                latch.countDown();
            }
        };
        for (ElasticGridServiceAgentProvisioningProgressChangedEvent event : events) {
            eventManager.elasticGridServiceAgentProvisioningProgressChanged(event);
        }
        eventManager.add(listener);
        Assert.assertTrue(latch.await(EVENTS_TIMEOUT_SECONDS,TimeUnit.SECONDS));
        Assert.assertEquals("pu1,pu2,pu3",StringUtils.collectionToCommaDelimitedString(receivedEvents));
    }
    
    /**
     * Register to the events after the are fired, includeExisting = false
     */
    @Test
    public void testNotExistingEvents() throws InterruptedException {
        
        ElasticGridServiceAgentProvisioningProgressChangedEvent[] events = { 
                createEvent("pu1"),
                createEvent("pu2"),
                createEvent("pu3")
        };
        
        final List<String> receivedEvents = new ArrayList<String>();
        final CountDownLatch latch = new CountDownLatch(3);
        ElasticGridServiceAgentProvisioningProgressChangedEventListener listener = 
                new ElasticGridServiceAgentProvisioningProgressChangedEventListener() {
            
            @Override
            public void elasticGridServiceAgentProvisioningProgressChanged(
                    ElasticGridServiceAgentProvisioningProgressChangedEvent event) {
                receivedEvents.add(event.getProcessingUnitName());
                latch.countDown();
            }
        };
        for (ElasticGridServiceAgentProvisioningProgressChangedEvent event : events) {
            eventManager.elasticGridServiceAgentProvisioningProgressChanged(event);
        }
        eventManager.add(listener,false);
        Assert.assertFalse(latch.await(EVENTS_TIMEOUT_SECONDS,TimeUnit.SECONDS));
        for (ElasticGridServiceAgentProvisioningProgressChangedEvent event : events) {
            eventManager.elasticGridServiceAgentProvisioningProgressChanged(event);
        }
        Assert.assertTrue(latch.await(EVENTS_TIMEOUT_SECONDS,TimeUnit.SECONDS));
        eventManager.remove(listener);
        Assert.assertEquals("pu1,pu2,pu3",StringUtils.collectionToCommaDelimitedString(receivedEvents));
    }

    /**
     * Register to the events after they are fired, more than one event per pu, includeExisting = true
     */
    @Test
    public void testOnlyLastEvents() throws InterruptedException {
        
        ElasticGridServiceAgentProvisioningProgressChangedEvent[] events = { 
                createEvent(false, false, "pu1"), //this event gets overriden by the next pu1 event
                createEvent(false, false, "pu2"), //this event gets overriden by the next pu2 event
                createEvent(false, false,"pu3"),
                createEvent(false, true, "pu2"),
                createEvent(true , false,"pu1"),
        };
        
        final List<String> receivedEvents = new ArrayList<String>();
        final CountDownLatch latch = new CountDownLatch(3);
        ElasticGridServiceAgentProvisioningProgressChangedEventListener listener = 
                new ElasticGridServiceAgentProvisioningProgressChangedEventListener() {
            
            @Override
            public void elasticGridServiceAgentProvisioningProgressChanged(
                    ElasticGridServiceAgentProvisioningProgressChangedEvent event) {
                receivedEvents.add(event.isComplete()+"_"+event.isUndeploying()+"_"+event.getProcessingUnitName());
                latch.countDown();
            }
        };
 
        for (ElasticGridServiceAgentProvisioningProgressChangedEvent event : events) {
            eventManager.elasticGridServiceAgentProvisioningProgressChanged(event);
        }
        
        eventManager.add(listener);
        Assert.assertTrue(latch.await(EVENTS_TIMEOUT_SECONDS,TimeUnit.SECONDS));
        Assert.assertEquals("false_false_pu3,false_true_pu2,true_false_pu1",StringUtils.collectionToCommaDelimitedString(receivedEvents));
    }
}
