package org.openspaces.itest.executor.simple;

import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.gigaspaces.async.*;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.AutowireTask;
import org.openspaces.core.executor.AutowireTaskMarker;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.Task;
import org.openspaces.core.executor.TaskGigaSpace;
import org.openspaces.core.executor.support.AvgTask;
import org.openspaces.core.executor.support.MaxTask;
import org.openspaces.core.executor.support.MinTask;
import org.openspaces.core.executor.support.SumTask;
import org.openspaces.core.executor.support.WaitForAllListener;
import org.openspaces.core.executor.support.WaitForAnyListener;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kimchy
 */
public class SimpleExecutorTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace1;

    protected GigaSpace clusteredGigaSpace1;

    protected GigaSpace gigaSpace2;

    protected GigaSpace clusteredGigaSpace2;

    public SimpleExecutorTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/executor/simple/context.xml"};
    }

    protected void onSetUp() throws Exception {
        gigaSpace1.clear(null);
        gigaSpace2.clear(null);
    }

    protected void onTearDown() throws Exception {
        gigaSpace1.clear(null);
        gigaSpace2.clear(null);
    }

    public void testSimpleTaskExecution() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new MyTask());
        assertEquals(1, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testSimpleTaskExecutionWithRouting() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new MyTask2(), 1);
        assertEquals(1, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testMultiRoutingExecution() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new MyDistributedTask1(), 1, 2);
        assertEquals(2, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testException1() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new TaskException(), 1);
        try {
            result.get(1000, TimeUnit.MILLISECONDS);
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    public void testExceptionListener1() throws Exception {
        WaitForAllListener<Integer> listener = new WaitForAllListener<Integer>(1);
        clusteredGigaSpace1.execute(new TaskException(), 1, listener);
        Future[] results = listener.waitForResult(1000, TimeUnit.MILLISECONDS);
        assertEquals(1, results.length);
        try {
            results[0].get(1000, TimeUnit.MILLISECONDS);
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    public void testMultiRoutingExecutionWithModeratorAll() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new AggregatorContinue(), 1, 2);
        assertEquals(2, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testMultiRoutingExecutionWithModeratorBreak() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new AggregatorBreak(), 1, 2);
        assertEquals(1, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testBroadcastExecutionWithModeratorAll() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new AggregatorContinue());
        assertEquals(2, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testBroadcastExecutionWithModeratorBreak() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new AggregatorBreak());
        assertEquals(1, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testExecutorBuilder1() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.executorBuilder(new AggregatorContinue())
                .add(new Task1(), 1).add(new Task1(), 2).add(new Task1(), 3).execute();
        assertEquals(3, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testExecutorBuilder2() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.executorBuilder(new AggregatorContinue())
                .add(new Task1Routing(1)).add(new Task1Routing(2)).add(new Task1Routing(2)).execute();
        assertEquals(3, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testExecutorBuilder3() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.executorBuilder(new AggregatorContinue())
                .add(new Task1Routing(1)).add(new AggregatorContinue(), 1, 2).add(new Task1Routing(2)).execute();
        assertEquals(4, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testExecutorBuilder4() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.executorBuilder(new AggregatorContinue())
                .add(new Task1Routing(1)).add(new AggregatorContinue()).add(new Task1Routing(2)).execute();
        assertEquals(4, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testExecutorBuilder5() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.executorBuilder(new AggregatorBreak())
                .add(new Task1Routing(1)).add(new AggregatorContinue()).add(new Task1Routing(2)).execute();
        assertEquals(1, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testBroadcastExecution() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new MyDistributedTask2());
        assertEquals(2, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testDistributedExecutionWithListener() throws Exception {
        WaitForAllListener<Integer> listener = new WaitForAllListener<Integer>(1);
        clusteredGigaSpace1.execute(new MyDistributedTask(), listener);
        Future<Integer>[] results = listener.waitForResult(500, TimeUnit.MILLISECONDS);
        assertEquals(1, results.length);
        assertEquals(2, (int) results[0].get());
    }

    public void testInjection() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new MyDistributedTask());
        assertEquals(2, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testInjection2() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new ApplicationContextInjectable());
        assertEquals(2, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testInjection3() throws TimeoutException, ExecutionException, InterruptedException {
        AsyncFuture result = clusteredGigaSpace1.execute(new MyTask3(), 1);
        assertNull(result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testInjection4() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new TaskGigaSpaceInjectable());
        assertEquals(2, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testIntegerSumTask() throws Exception {
        AsyncFuture<Long> result = clusteredGigaSpace1.execute(new SumTask<Integer, Long>(Long.class, new Task1()));
        assertEquals(2, (long) result.get(100, TimeUnit.MILLISECONDS));
    }

    public void testIntegerMaxTask() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new MaxTask<Integer>(Integer.class, new IncrementalTask()));
        assertEquals(2, (int) result.get(100, TimeUnit.MILLISECONDS));
    }

    public void testIntegerMinTask() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new MinTask<Integer>(Integer.class, new IncrementalTask()));
        assertEquals(1, (int) result.get(100, TimeUnit.MILLISECONDS));
    }

    public void testAvgTask() throws Exception {
        AsyncFuture<Float> result = clusteredGigaSpace1.execute(new AvgTask<Integer, Float>(Float.class, new IncrementalTask()));
        assertEquals(1.5, result.get(100, TimeUnit.MILLISECONDS), 0.000001);
    }

    public void testWaitForAnyListener() throws Exception {
        WaitForAnyListener<Integer> listener = new WaitForAnyListener<Integer>(2);
        AsyncFuture<Integer> future = clusteredGigaSpace1.execute(new DelayedTask(500));
        future.setListener(listener);
        future = clusteredGigaSpace1.execute(new DelayedTask(100));
        future.setListener(listener);
        assertEquals(100, (int) listener.waitForResult());
    }

    public void testWaitForAllListener() throws Exception {
        WaitForAllListener<Integer> listener = new WaitForAllListener<Integer>(2);
        AsyncFuture<Integer> future = clusteredGigaSpace1.execute(new DelayedTask(500));
        future.setListener(listener);
        future = clusteredGigaSpace1.execute(new DelayedTask(100));
        future.setListener(listener);
        assertEquals(2, listener.waitForResult().length);
        assertEquals(100, (int) listener.waitForResult()[0].get());
        assertEquals(500, (int) listener.waitForResult()[1].get());
    }

    private static class DelayedTask implements Task<Integer> {

        private int waitTime;

        public DelayedTask(int waitTime) {
            this.waitTime = waitTime;
        }

        @SpaceRouting
        public int routing() {
            return waitTime;
        }

        public Integer execute() throws Exception {
            Thread.sleep(waitTime);
            return waitTime;
        }
    }

    private static class Task1 implements Task<Integer> {
        public Integer execute() throws Exception {
            return 1;
        }
    }

    private static class TaskException implements Task<Integer> {
        public Integer execute() throws Exception {
            throw new IllegalArgumentException("test");
        }
    }

    private static class IncrementalTask implements Task<Integer> {

        private AtomicInteger val = new AtomicInteger();

        public Integer execute() throws Exception {
            return val.incrementAndGet();
        }
    }

    private static class Task1Routing implements Task<Integer> {

        private int routing;

        public Task1Routing(int routing) {
            this.routing = routing;
        }

        @SpaceRouting
        public int routing() {
            return routing;
        }

        public Integer execute() throws Exception {
            return 1;
        }
    }

    private static class AggregatorContinue implements DistributedTask<Integer, Integer>, AsyncResultFilter<Integer> {

        public Integer execute() throws Exception {
            return 1;
        }

        public Integer reduce(List<AsyncResult<Integer>> asyncResults) {
            int sum = 0;
            for (AsyncResult<Integer> res : asyncResults) {
                sum += res.getResult();
            }
            return sum;
        }

        public Decision onResult(AsyncResultFilterEvent<Integer> event) {
            return Decision.CONTINUE;
        }
    }

    private static class AggregatorBreak implements DistributedTask<Integer, Integer>, AsyncResultFilter<Integer> {

        public Integer execute() throws Exception {
            return 1;
        }

        public Integer reduce(List<AsyncResult<Integer>> asyncResults) {
            int sum = 0;
            for (AsyncResult<Integer> res : asyncResults) {
                sum += res.getResult();
            }
            return sum;
        }

        public Decision onResult(AsyncResultFilterEvent<Integer> event) {
            if (event.getReceivedResults().length == 0) {
                return Decision.BREAK;
            }
            return Decision.CONTINUE;
        }
    }

    @AutowireTask
    private static class ApplicationContextInjectable extends AggregatorContinue implements ApplicationContextAware {

        private transient ApplicationContext applicationContext;

        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }

        public Integer execute() throws Exception {
            if (applicationContext == null) {
                throw new Exception();
            }
            return super.execute();
        }
    }

    public static class TaskGigaSpaceInjectable extends AggregatorContinue {

        @TaskGigaSpace
        public transient GigaSpace gigaSpace;

        public Integer execute() throws Exception {
            if (gigaSpace == null) {
                throw new Exception();
            }
            return super.execute();
        }
    }

    private static class MyDistributedTask implements DistributedTask<Integer, Integer>, AutowireTaskMarker {

        @Resource(name = "gigaSpace1")
        transient GigaSpace gigaSpace;

        public Integer execute() throws Exception {
            if (gigaSpace == null) {
                throw new Exception();
            }
            return 1;
        }

        public Integer reduce(List<AsyncResult<Integer>> asyncResults) throws Exception {
            int sum = 0;
            for (AsyncResult<Integer> res : asyncResults) {
                if (res.getException() != null) {
                    throw res.getException();
                }
                sum += res.getResult();
            }
            return sum;
        }
    }

    private static class MyTask implements Task<Integer> {

        @SpaceRouting
        public int routing() {
            return 1;
        }

        public Integer execute() throws Exception {
            return 1;
        }
    }

    private static class MyTask2 implements Task<Integer> {
        public Integer execute() throws Exception {
            return 1;
        }
    }

    private static class MyDistributedTask1 implements DistributedTask<Integer, Integer> {
        public Integer execute() throws Exception {
            return 1;
        }

        public Integer reduce(List<AsyncResult<Integer>> asyncResults) {
            int sum = 0;
            for (AsyncResult<Integer> res : asyncResults) {
                sum += res.getResult();
            }
            return sum;
        }
    }

    private static class MyDistributedTask2 implements DistributedTask<Integer, Integer> {
        public Integer execute() throws Exception {
            return 1;
        }

        public Integer reduce(List<AsyncResult<Integer>> asyncResults) {
            int sum = 0;
            for (AsyncResult<Integer> res : asyncResults) {
                sum += res.getResult();
            }
            return sum;
        }
    }

    private static class MyTask3 implements Task {

        @TaskGigaSpace
            private GigaSpace gigaSpace;

        public Serializable execute() throws Exception {
            if (gigaSpace == null) {
                throw new NullPointerException();
            }
            return null;
        }
    }

    private static class MyTask4 implements Task<Integer> {
        public Integer execute() throws Exception {
            return 1;
        }
    }
}
