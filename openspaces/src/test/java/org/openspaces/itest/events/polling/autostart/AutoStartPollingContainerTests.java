/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.itest.events.polling.autostart;

import org.openspaces.core.GigaSpace;
import org.springframework.context.Lifecycle;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class AutoStartPollingContainerTests extends AbstractDependencyInjectionSpringContextTests {


    protected GigaSpace gigaSpace;
    protected Lifecycle pollingContainer ;
    protected AutoStartEventListener autoStartEventListener;

    public AutoStartPollingContainerTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/events/polling/autostart/polling-autostart.xml"};
    }


    public void testAutoStartFalse() throws Exception{
        assertEquals(0, autoStartEventListener.getMessageCounter());
        gigaSpace.write(new Object());
        Thread.sleep(500);
        assertEquals(0, autoStartEventListener.getMessageCounter());
        pollingContainer.start() ;
        Thread.sleep(500);
        assertEquals(1, autoStartEventListener.getMessageCounter());        
    }

}