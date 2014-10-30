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

package org.openspaces.itest.events.polling.exceptionhandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.openspaces.itest.utils.EmptySpaceDataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/events/polling/exceptionhandler/polling-exceptionhandler.xml")
public class ExceptionHandlerPollingContainerTests   { 


     @Autowired protected GigaSpace gigaSpace;
     @Autowired protected TestEventListener eventListener;
     @Autowired protected TestExceptionHandler exceptionHandler;

    public ExceptionHandlerPollingContainerTests() {
 
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/events/polling/exceptionhandler/polling-exceptionhandler.xml"};
    }


     @Test public void testExceptionHandler() throws Exception{
        exceptionHandler.reset();
        eventListener.reset();
        assertEquals(0, eventListener.getMessageCounter());
        gigaSpace.write(new EmptySpaceDataObject());
        Thread.sleep(500);
        assertEquals(1, eventListener.getMessageCounter());
        assertTrue(exceptionHandler.isSuccess());
        assertEquals(0, exceptionHandler.getFailureCount());

        exceptionHandler.reset();
        eventListener.reset();
        eventListener.setThrowExceptionTillCounter(2);
        assertEquals(0, eventListener.getMessageCounter());

        gigaSpace.write(new EmptySpaceDataObject());
        Thread.sleep(500);
        assertEquals(2, eventListener.getMessageCounter());
        assertTrue(exceptionHandler.isSuccess());
        assertEquals(1, exceptionHandler.getFailureCount());
    }

}

