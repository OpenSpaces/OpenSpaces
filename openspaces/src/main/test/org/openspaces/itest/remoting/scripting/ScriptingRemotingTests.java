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

package org.openspaces.itest.remoting.scripting;

import org.openspaces.core.GigaSpace;
import org.openspaces.remoting.scripting.*;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public class ScriptingRemotingTests extends AbstractDependencyInjectionSpringContextTests {

    protected ScriptingExecutor asyncScriptingExecutor;

    protected ScriptingExecutor executorScriptingExecutor;

    protected ScriptingAnnotationBean scriptingAnnotationBean;

    protected GigaSpace gigaSpace;

    public ScriptingRemotingTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/remoting/scripting/scripting-remoting.xml"};
    }


    public void testAsyncSyncExecution() {
        Integer value = (Integer) asyncScriptingExecutor.execute(new StaticScript("testAsyncSyncExecution", "groovy", "return 1"));
        assertEquals(1, value.intValue());
    }

    public void testAsyncAsyncExcution() throws ExecutionException, InterruptedException {
        Future<Integer> result = asyncScriptingExecutor.asyncExecute(new StaticScript("testAsyncAsycnExcution", "groovy", "return 1"));
        assertEquals(1, result.get().intValue());
    }

    public void testExecutorSyncExecution() {
        Integer value = (Integer) executorScriptingExecutor.execute(new StaticScript("testAsyncSyncExecution", "groovy", "return 1"));
        assertEquals(1, value.intValue());
    }

    public void testExecutorAsyncExcution() throws ExecutionException, InterruptedException {
        Future<Integer> result = executorScriptingExecutor.asyncExecute(new StaticScript("testAsyncAsycnExcution", "groovy", "return 1"));
        assertEquals(1, result.get().intValue());
    }

    public void testAsyncSyncExecutionWithReferenceToSpace() {
        gigaSpace.clear(null);
        asyncScriptingExecutor.execute(new StaticScript("testAsyncSyncExecutionWithReferenceToSpace", "groovy", "gigaSpace.write(new Object())"));
        assertEquals(1, gigaSpace.count(new Object()));
    }

    public void testSyncJsr223Execution() {
        Double value = (Double) executorScriptingExecutor.execute(new StaticScript("testSyncJsr223Execution", "JavaScript", "1"));
        assertEquals(1, value.intValue());
    }

    public void testSyncJsr223WithParameterExecution() {
        Integer value = (Integer) executorScriptingExecutor.execute(new StaticScript("testSyncJsr223WithParameterExecution", "JavaScript", "number").parameter("number", 1));
        assertEquals(1, value.intValue());
    }

    public void testSyncJRubyExecution() {
        Long value = (Long) executorScriptingExecutor.execute(new StaticScript("testSyncJRubyExecution", "ruby", "1"));
        assertEquals(1, value.intValue());
    }

    public void testSyncJRubyWithParameteresExecution() {
        Long value = (Long) executorScriptingExecutor.execute(new StaticScript("testSyncJRubyWithParameteresExecution", "ruby", "$number").parameter("number", 1));
        assertEquals(1, value.intValue());
    }

    public void testSimpleCachingWithJRuby() {
        // warmup
        Long value = (Long) executorScriptingExecutor.execute(new StaticScript("testSimpleCachingWithJRuby-warmup", "ruby", "$number").parameter("number", 1));
        assertEquals(1, value.intValue());

        long time = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            value = (Long) executorScriptingExecutor.execute(new StaticScript("testSimpleCachingWithJRuby-cached", "ruby", "$number").parameter("number", 1));
            assertEquals(1, value.intValue());
        }
        long cacheTime = System.nanoTime() - time;
        time = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            value = (Long) executorScriptingExecutor.execute(new StaticScript("testSimpleCachingWithJRuby-not-cached", "ruby", "$number").parameter("number", 1).cache(false));
            assertEquals(1, value.intValue());
        }
        long nonCacheTime = System.nanoTime() - time;
        assertTrue(cacheTime < nonCacheTime);
    }

    public void testLazyLoadingGroovyScript() {
        gigaSpace.clear(null);
        Integer value = (Integer) executorScriptingExecutor.execute(new ResourceLazyLoadingScript("testLazyLoadingGroovyScript", "groovy", "classpath:/org/openspaces/itest/remoting/scripting/test.groovy"));
        assertEquals(1, value.intValue());
        value = (Integer) executorScriptingExecutor.execute(new ResourceLazyLoadingScript("testLazyLoadingGroovyScript", "groovy", "classpath:/org/openspaces/itest/remoting/scripting/test.groovy"));
        assertEquals(2, value.intValue());
    }

    public void testAnnotationInjection() {
        assertEquals(2, scriptingAnnotationBean.executeEventScriptThatReturns2().intValue());
        assertEquals(2, scriptingAnnotationBean.executeExecutorScriptThatReturns2().intValue());
    }

    public void testEventScriptingConfigurer() {
        ScriptingExecutor<Integer> executor = new EventDrivenScriptingProxyConfigurer<Integer>(gigaSpace)
                .timeout(15000)
                .scriptingExecutor();
        Integer result = executor.execute(new StaticScript().type("groovy").name("testAsyncScriptingConfigurer").script("return 1"));
        assertEquals(1, result.intValue());
    }

    public void testExecutorScriptingConfigurer() {
        ScriptingExecutor<Integer> executor = new ExecutorScriptingProxyConfigurer<Integer>(gigaSpace)
                .scriptingExecutor();
        Integer result = executor.execute(new StaticScript().type("groovy").name("testSyncScriptingConfigurer").script("return 1"));
        assertEquals(1, result.intValue());
    }
}