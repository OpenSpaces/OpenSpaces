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

package org.openspaces.itest.remoting.simple;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public class SimpleRemotingTests extends AbstractDependencyInjectionSpringContextTests {

    protected SimpleService simpleService;

    protected SimpleServiceAsync simpleServiceAsync;

    protected SimpleService simpleServiceSync;

    protected GigaSpace gigaSpace;

    public SimpleRemotingTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/remoting/simple/simple-remoting.xml"};
    }


    public void testAsyncSyncExecution() {
        String reply = simpleService.say("test");
        assertEquals("SAY test", reply);
    }

    public void testAsyncExecotionIsDone() throws InterruptedException, ExecutionException {
        Future result = simpleService.asyncSay("test");
        while (!result.isDone()) {
            Thread.sleep(1000);
        }
        assertEquals("SAY test", result.get());
    }

    public void testAsyncExecutionGet() throws ExecutionException, InterruptedException {
        Future result = simpleService.asyncSay("test");
        assertEquals("SAY test", result.get());
    }

    public void testAsyncExecutionGetWithAsyncInterface() throws ExecutionException, InterruptedException {
        Future result = simpleServiceAsync.say("test");
        assertEquals("SAY test", result.get());
    }

    public void testSyncSyncExecution() {
        String reply = simpleServiceSync.say("test");
        assertEquals("SAY test", reply);
    }
}