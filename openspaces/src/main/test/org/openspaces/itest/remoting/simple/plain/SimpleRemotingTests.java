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

package org.openspaces.itest.remoting.simple.plain;

import com.gigaspaces.async.AsyncFuture;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.support.WaitForAnyListener;
import org.openspaces.remoting.EventDrivenRemotingProxyConfigurer;
import org.openspaces.remoting.ExecutorRemotingProxyConfigurer;
import org.openspaces.remoting.HashedSpaceRemotingEntry;
import org.openspaces.remoting.SpaceRemotingEntry;
import org.openspaces.remoting.SpaceRemotingEntryFactory;
import org.openspaces.remoting.SpaceRemotingEntryMetadataFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public class SimpleRemotingTests extends AbstractDependencyInjectionSpringContextTests {

    protected SimpleService simpleService;

    protected SuperSimpleService superSimpleService;

    protected SimpleServiceAsync simpleServiceAsync;

    protected SimpleService simpleServiceExecutor;

    protected SimpleAnnotationBean simpleAnnotationBean;

    protected GigaSpace gigaSpace;

    public SimpleRemotingTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/remoting/simple/plain/simple-remoting.xml"};
    }


    public void testAsyncSyncExecution() {
        String reply = simpleService.say("test");
        assertEquals("SAY test", reply);
        String reply1 = simpleService.superSay("test");
        assertEquals("Super SAY test", reply1);
    }

    public void testSuperAsyncSyncExecution() {
        String reply = superSimpleService.superSay("test");
        assertEquals("Super SAY test", reply);
    }

    public void testAsyncSyncExecutionWithException() {
        try {
            simpleService.testException();
            fail();
        } catch (SimpleService.MyException e) {
            // all is well
        }
    }

    public void testAsyncAsyncExecutionWithException() {
        try {
            simpleService.asyncTestException().get();
            fail();
        } catch (InterruptedException e) {
            fail();
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof SimpleService.MyException)) {
                fail();
            }
        }
    }

    public void testAsyncExecutionIsDone() throws InterruptedException, ExecutionException {
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

    public void testExecutorSyncExecution() {
        String reply = simpleServiceExecutor.say("test");
        assertEquals("SAY test", reply);
    }

    public void testExecutorAsyncExecution() throws Exception {
        Future<String> reply = simpleServiceExecutor.asyncSay("test");
        assertEquals("SAY test", reply.get());
    }

    public void testExecutorAsyncExecutionWithListener() throws Exception {
        AsyncFuture<String> reply = (AsyncFuture<String>) simpleServiceExecutor.asyncSay("test");
        WaitForAnyListener<String> listener = new WaitForAnyListener<String>(1);
        reply.setListener(listener);
        assertEquals("SAY test", listener.waitForResult());
    }

    public void testExecutorSyncExecutionWithException() {
        try {
            simpleServiceExecutor.testException();
            fail();
        } catch (SimpleService.MyException e) {
            // all is well
        }
    }

    public void testExecutorAsyncExecutionWithException() {
        try {
            simpleServiceExecutor.asyncTestException().get();
            fail();
        } catch (InterruptedException e) {
            fail();
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof SimpleService.MyException)) {
                fail();
            }
        }
    }

    public void testOverloaded() throws Exception {
        String result = simpleAnnotationBean.executorSimpleService.overloaded(Arrays.asList("test", "something"));
        assertEquals("L2", result);
        result = simpleAnnotationBean.executorSimpleService.asyncOverloaded(Arrays.asList("test", "something")).get();
        assertEquals("L2", result);

        Map<String, String> map = new HashMap<String, String>();
        map.put("test", "value");
        result = simpleAnnotationBean.executorSimpleService.overloaded(map);
        assertEquals("M1", result);
        result = simpleAnnotationBean.executorSimpleService.asyncOverloaded(map).get();
        assertEquals("M1", result);
    }

    public void testSimpleAnnotationExecution() {
        String reply = simpleAnnotationBean.eventSimpleService.say("test");
        assertEquals("SAY test", reply);
        reply = simpleAnnotationBean.executorSimpleService.say("test");
        assertEquals("SAY test", reply);
    }

    public void testWiredParameters() {
        assertTrue(simpleAnnotationBean.executorSimpleService.wire(new WiredParameter()));
        assertTrue(simpleAnnotationBean.eventSimpleService.wire(new WiredParameter()));
    }

    public void testSimpleConfigurerExecution() {
        SimpleService localSyncSimpleService = new ExecutorRemotingProxyConfigurer<SimpleService>(gigaSpace, SimpleService.class)
                .proxy();
        String reply = localSyncSimpleService.say("test");
        assertEquals("SAY test", reply);
        SimpleService localAsyncSimpleService = new EventDrivenRemotingProxyConfigurer<SimpleService>(gigaSpace, SimpleService.class)
                .timeout(15000)
                .proxy();
        reply = localAsyncSimpleService.say("test");
        assertEquals("SAY test", reply);
    }

    public void testSerializationOfAsyncRemotingEntry() throws IOException, ClassNotFoundException {
        SpaceRemotingEntryFactory remotingEntryFactory = new SpaceRemotingEntryMetadataFactory();
		HashedSpaceRemotingEntry entry = remotingEntryFactory.createHashEntry();
        entry = entry.buildInvocation("test", "test", null, null);
        entry.setOneWay(true);
        entry.setMetaArguments(new Object[]{new Integer(1)});
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
        oos.writeObject(entry);
        oos.flush();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(byteArrayInputStream);
        SpaceRemotingEntry invocation = (SpaceRemotingEntry) ois.readObject();
        compareAsyncInvocationNullableFields(entry, invocation);

        entry = (HashedSpaceRemotingEntry) entry.buildResult("result");
        entry.setInstanceId(1);
        byteArrayOutputStream = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(byteArrayOutputStream);
        oos.writeObject(entry);
        oos.flush();
        bytes = byteArrayOutputStream.toByteArray();
        byteArrayInputStream = new ByteArrayInputStream(bytes);
        ois = new ObjectInputStream(byteArrayInputStream);
        SpaceRemotingEntry invocationResult = (SpaceRemotingEntry) ois.readObject();
        compareAsyncResultNullableFields(entry, invocationResult);
    }

    private void compareAsyncResultNullableFields(SpaceRemotingEntry entry, SpaceRemotingEntry invocationResult) {
        assertEquals(entry.getRouting(), invocationResult.getRouting());
        assertEquals(entry.getResult(), invocationResult.getResult());
        assertEquals(entry.getInstanceId(), invocationResult.getInstanceId());
        assertEquals(entry.getException(), invocationResult.getException());
    }

    private void compareAsyncInvocationNullableFields(SpaceRemotingEntry entry, SpaceRemotingEntry invocation) {
        assertEquals(entry.getRouting(), invocation.getRouting());
        assertEquals(entry.getOneWay(), invocation.getOneWay());
        Object[] entryMetaArgs = entry.getMetaArguments();
        if (entryMetaArgs != null) {
            Object[] invocationMetaArgs = invocation.getMetaArguments();
            for (int i = 0; i < entryMetaArgs.length; i++) {
                assertEquals(entryMetaArgs[i], invocationMetaArgs[i]);
            }
        } else {
            assertNull(invocation.getMetaArguments());
        }
        Object[] entryArgs = entry.getArguments();
        if (entryArgs != null) {
            Object[] invocationArgs = invocation.getArguments();
            for (int i = 0; i < entryArgs.length; i++) {
                assertEquals(entryArgs[i], invocationArgs[i]);
            }
        } else {
            assertNull(invocation.getArguments());
        }
    }
}