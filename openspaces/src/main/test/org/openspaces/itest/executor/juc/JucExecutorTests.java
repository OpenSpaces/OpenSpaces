package org.openspaces.itest.executor.juc;

import com.gigaspaces.annotation.pojo.SpaceRouting;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.TaskExecutors;
import org.openspaces.core.executor.juc.TaskExecutorService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

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
        Future<Integer> result = executorService.submit(new Callable<Integer>() {

            @SpaceRouting
            public int routing() {
                return 1;
            }

            public Integer call() throws Exception {
                return 1;
            }
        });
        assertEquals(1, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }

    public void testSimpleRunnableExecution() throws Exception {
        Future<Integer> result = executorService.submit(new Runnable() {

            @SpaceRouting
            public int routing() {
                return 1;
            }

            public void run() {
            }
        }, 1);
        assertEquals(1, (int) result.get(1000, TimeUnit.MILLISECONDS));
    }
}