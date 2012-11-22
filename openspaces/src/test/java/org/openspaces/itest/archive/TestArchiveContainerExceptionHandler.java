/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.itest.archive;

import org.junit.Assert;
import org.junit.Test;
import org.openspaces.archive.DefaultArchivePollingContainerExceptionHandler;
import org.openspaces.events.ListenerExecutionFailedException;

/**
 * @author Itai Frenkel
 * @since 9.1.1
 */
public class TestArchiveContainerExceptionHandler {

    @Test
    public void testFilteringByMessage() {
        
        DefaultArchivePollingContainerExceptionHandler handler = new DefaultArchivePollingContainerExceptionHandler();
        
        final ListenerExecutionFailedException e1 = new ListenerExecutionFailedException("",new Exception("e1"));
        final ListenerExecutionFailedException e2 = new ListenerExecutionFailedException("",new Exception("e2"));
        long now = System.currentTimeMillis();
        Assert.assertTrue(handler.shouldLog(e1, now));
        handler.storeLastException(e1, now);
        Assert.assertFalse(handler.shouldLog(e1, now));
        Assert.assertTrue(handler.shouldLog(e2, now));
    }
    
    @Test
    public void testFilteringByTime() {
        
        DefaultArchivePollingContainerExceptionHandler handler = new DefaultArchivePollingContainerExceptionHandler();
        
        final ListenerExecutionFailedException e1 = new ListenerExecutionFailedException("",new Exception("e1"));
        long now = System.currentTimeMillis();
        Assert.assertTrue(handler.shouldLog(e1, now));
        handler.storeLastException(e1, now);
        Assert.assertFalse(handler.shouldLog(e1, now+1));
        Assert.assertTrue(handler.shouldLog(e1, now+60001));
    }
    
    @Test
    public void testFilteringByType() {
        
        DefaultArchivePollingContainerExceptionHandler handler = new DefaultArchivePollingContainerExceptionHandler();
        
        final ListenerExecutionFailedException e1 = new ListenerExecutionFailedException("",new Exception("e1"));
        final ListenerExecutionFailedException e2 = new ListenerExecutionFailedException("",new RuntimeException("e1"));
        long now = System.currentTimeMillis();
        Assert.assertTrue(handler.shouldLog(e1, now));
        handler.storeLastException(e1, now);
        Assert.assertFalse(handler.shouldLog(e1, now));
        Assert.assertTrue(handler.shouldLog(e2, now));
    }
    
    @Test
    public void testFilteringNullCause() {
        
        DefaultArchivePollingContainerExceptionHandler handler = new DefaultArchivePollingContainerExceptionHandler();
        
        final ListenerExecutionFailedException e1 = new ListenerExecutionFailedException("",null);
        long now = System.currentTimeMillis();
        Assert.assertTrue(handler.shouldLog(e1, now));
        handler.storeLastException(e1, now);
        Assert.assertFalse(handler.shouldLog(e1, now));
        Assert.assertFalse(handler.shouldLog(e1, now+59999));
        Assert.assertTrue(handler.shouldLog(e1, now+60001));
    }
    
    @Test
    public void testFilteringNullMessage() {
        
        DefaultArchivePollingContainerExceptionHandler handler = new DefaultArchivePollingContainerExceptionHandler();
        
        final ListenerExecutionFailedException e1 = new ListenerExecutionFailedException("",new Exception((String)null));
        long now = System.currentTimeMillis();
        Assert.assertTrue(handler.shouldLog(e1, now));
        handler.storeLastException(e1, now);
        Assert.assertFalse(handler.shouldLog(e1, now));
        Assert.assertTrue(handler.shouldLog(e1, now+60001));
    }
}
