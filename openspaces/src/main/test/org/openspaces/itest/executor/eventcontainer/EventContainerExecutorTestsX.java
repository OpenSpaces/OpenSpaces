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

import com.gigaspaces.async.AsyncFuture;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.support.RegisterEventContainerTask;
import org.openspaces.events.support.UnregisterEventContainerTask;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class EventContainerExecutorTestsX extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace1;

    protected GigaSpace gigaSpace2;

    protected GigaSpace distGigaSpace;

    public EventContainerExecutorTestsX() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/executor/eventcontainer/context.xml"};
    }

    protected void onSetUp() throws Exception {
        distGigaSpace.clear(null);
    }

    protected void onTearDown() throws Exception {
        distGigaSpace.clear(null);
    }

    public void testDynamicRegistrationOfEvents() throws Exception {
        DynamicEventListener listener = new DynamicEventListener();
        gigaSpace1.write(new Object());
        Thread.sleep(200);
        assertFalse(listener.isReceivedEvent());
        AsyncFuture future = distGigaSpace.execute(new RegisterEventContainerTask(listener), 0);
        future.get(500, TimeUnit.MILLISECONDS);
        Thread.sleep(500);
        assertTrue(listener.isReceivedEvent());

        listener.setReceivedEvent(false);
        future = distGigaSpace.execute(new UnregisterEventContainerTask("test"), 0);
        future.get(500, TimeUnit.MILLISECONDS);
        gigaSpace1.write(new Object());
        Thread.sleep(500);
        assertFalse(listener.isReceivedEvent());
    }

}
