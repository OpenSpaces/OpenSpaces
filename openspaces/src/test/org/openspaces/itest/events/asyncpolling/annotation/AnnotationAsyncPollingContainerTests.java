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

package org.openspaces.itest.events.asyncpolling.annotation;

import org.openspaces.core.GigaSpace;
import org.openspaces.itest.core.simple.Message;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class AnnotationAsyncPollingContainerTests extends AbstractDependencyInjectionSpringContextTests {


    protected GigaSpace gigaSpace;

    protected TestListener testListener;

    public AnnotationAsyncPollingContainerTests() {
        setPopulateProtectedVariables(true);
    }

    protected void onSetUp() throws Exception {
        gigaSpace.clear(null);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/events/asyncpolling/annotation/async-polling-annotation.xml"};
    }


    public void testReceiveMessage() throws Exception {
        assertFalse(testListener.isReceivedMessage());
        gigaSpace.write(new Message("test"));
        Thread.sleep(500);
        assertTrue(testListener.isReceivedMessage());
        testListener.setReceivedMessage(false);
        assertFalse(testListener.isReceivedMessage());
        gigaSpace.write(new Message("test2"));
        Thread.sleep(500);
        assertTrue(testListener.isReceivedMessage());
    }

}