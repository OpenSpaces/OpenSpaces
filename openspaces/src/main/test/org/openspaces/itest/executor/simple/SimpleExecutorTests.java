package org.openspaces.itest.executor.simple;

import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncResult;
import com.gigaspaces.async.AsyncResultFilter;
import com.gigaspaces.async.AsyncResultFilterEvent;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.Task;
import org.openspaces.core.executor.support.SumIntegerTask;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new Task<Integer>() {

            @SpaceRouting
            public int routing() {
                return 1;
            }

            public Integer execute() throws Exception {
                return 1;
            }
        });
        assertEquals(1, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testSimpleTaskExecutionNoRoutingException() throws Exception {
        try {
            AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new Task<Integer>() {
                public Integer execute() throws Exception {
                    return 1;
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // all is well
        }
    }

    public void testSimpleTaskExecutionWithRouting() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new Task<Integer>() {
            public Integer execute() throws Exception {
                return 1;
            }
        }, 1);
        assertEquals(1, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testMultiRoutingExecution() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new DistributedTask<Integer, Integer>() {
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
        }, 1, 2);
        assertEquals(2, (int) result.get(1000, TimeUnit.MILLISECONDS));
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
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new DistributedTask<Integer, Integer>() {
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
        });
        assertEquals(2, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testInjection() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new DistributedTask<Integer, Integer>() {

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
        });
        assertEquals(2, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testInjection2() throws Exception {
        AsyncFuture<Integer> result = clusteredGigaSpace1.execute(new ApplicationContextInjectable());
        assertEquals(2, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testIntegerSumTask() throws Exception {
        AsyncFuture<Long> result = clusteredGigaSpace1.execute(new SumIntegerTask(new Task1()));
        assertEquals(2, (long) result.get(100, TimeUnit.MILLISECONDS));
    }

    private class Task1 implements Task<Integer> {
        public Integer execute() throws Exception {
            return 1;
        }
    }

    private class Task1Routing implements Task<Integer> {

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

    private class AggregatorContinue implements DistributedTask<Integer, Integer>, AsyncResultFilter<Integer> {

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

    private class AggregatorBreak implements DistributedTask<Integer, Integer>, AsyncResultFilter<Integer> {

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
            if (event.getReceivedResults().size() == 0) {
                return Decision.BREAK;
            }
            return Decision.CONTINUE;
        }
    }

    private class ApplicationContextInjectable extends AggregatorContinue implements ApplicationContextAware {

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
}
