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
package org.openspaces.itest.executor.juc;

import com.gigaspaces.annotation.pojo.SpaceRouting;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.TaskExecutors;
import org.openspaces.core.executor.juc.TaskExecutorService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class JucExecutorTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace1;

    protected GigaSpace clusteredGigaSpace1;

    protected GigaSpace gigaSpace2;

    protected GigaSpace clusteredGigaSpace2;

    private TaskExecutorService executorService;

    public JucExecutorTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/executor/juc/context.xml"};
    }

    protected void onSetUp() throws Exception {
        gigaSpace1.clear(null);
        gigaSpace2.clear(null);
        this.executorService = TaskExecutors.newExecutorService(clusteredGigaSpace1);
    }

    protected void onTearDown() throws Exception {
        gigaSpace1.clear(null);
        gigaSpace2.clear(null);
    }

    public void testSimpleCallableExecution() throws Exception {
        Future<Integer> result = executorService.submit(new MyCallable1());
        assertEquals(1, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testSimpleRunnableExecution() throws Exception {
        Future<Integer> result = executorService.submit(new MyRunnable1(), 1);
        assertEquals(1, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    private static class MyCallable1 implements Callable<Integer>, Serializable {

        private static final long serialVersionUID = 2373484063403026358L;

        @SpaceRouting
        public int routing() {
            return 1;
        }

        public Integer call() throws Exception {
            return 1;
        }
    }

    private static class MyRunnable1 implements Runnable, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -7397964765805393730L;

        @SpaceRouting
        public int routing() {
            return 1;
        }

        public void run() {
        }
    }
}
