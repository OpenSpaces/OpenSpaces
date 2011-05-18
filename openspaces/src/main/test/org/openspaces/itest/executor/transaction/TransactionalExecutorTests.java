package org.openspaces.itest.executor.transaction;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResult;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.AutowireTask;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.Task;
import org.openspaces.core.executor.TaskGigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class TransactionalExecutorTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace localGigaSpace1;

    protected GigaSpace localGigaSpace2;

    protected GigaSpace distGigaSpace;

    protected PlatformTransactionManager localTxManager1;

    protected PlatformTransactionManager localTxManager2;

    protected PlatformTransactionManager distTxManager;

    public TransactionalExecutorTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/executor/transaction/context.xml"};
    }

    protected void onSetUp() throws Exception {
        localGigaSpace1.clear(null);
        localGigaSpace2.clear(null);
    }

    protected void onTearDown() throws Exception {
        localGigaSpace1.clear(null);
        localGigaSpace2.clear(null);
    }

    public void testSimpleTransactionCommit1() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager1);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AsyncFuture<Integer> value = localGigaSpace1.execute(new SimpleTask1(), 0);
                try {
                    assertEquals(1, (int) value.get(1000, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
            }
        });
        assertEquals(1, localGigaSpace1.count(null));
    }

    public void testSimpleTransactionCommit2() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager1);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AsyncFuture<Integer> value = localGigaSpace1.execute(new SimpleTask2(), 0);
                try {
                    assertEquals(1, (int) value.get(1000, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
            }
        });
        assertEquals(1, localGigaSpace1.count(null));
    }

    public void testSimpleTransactionRollback1() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager1);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AsyncFuture<Integer> value = localGigaSpace1.execute(new SimpleTask1(), 0);
                try {
                    assertEquals(1, (int) value.get(1000, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
                status.setRollbackOnly();
            }
        });
        assertEquals(0, localGigaSpace1.count(null));
    }

    public void testSimpleTransactionRollback2() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager1);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AsyncFuture<Integer> value = localGigaSpace1.execute(new SimpleTask2(), 0);
                try {
                    assertEquals(1, (int) value.get(1000, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
                status.setRollbackOnly();
            }
        });
        assertEquals(0, localGigaSpace1.count(null));
    }

    public void testListenerTransactionCommit() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager1);
        final Listener listener = new Listener();
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AsyncFuture<Integer> value = localGigaSpace1.execute(new DelayedSimpleTask1(), 0);
                value.setListener(listener);
            }
        });
        assertEquals(0, localGigaSpace1.count(null));
        while (listener.getResult() == null) {
            Thread.sleep(100);
        }
        // let it commit
        Thread.sleep(100);
        assertEquals(1, localGigaSpace1.count(null));
    }

    public void testListenerAsParameterTransactionCommit() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager1);
        final Listener listener = new Listener();
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AsyncFuture<Integer> value = localGigaSpace1.execute(new DelayedSimpleTask1(), 0, listener);
            }
        });
        assertEquals(0, localGigaSpace1.count(null));
        while (listener.getResult() == null) {
            Thread.sleep(100);
        }
        // let it commit
        Thread.sleep(100);
        assertEquals(1, localGigaSpace1.count(null));
    }

    public void testListenerTransactionRollback() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager1);
        final Listener listener = new Listener();
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AsyncFuture<Integer> value = localGigaSpace1.execute(new DelayedSimpleTask1(), 0);
                value.setListener(listener);
                status.setRollbackOnly();
            }
        });
        assertEquals(0, localGigaSpace1.count(null));
        while (listener.getResult() == null) {
            Thread.sleep(100);
        }
        // let it "commit"
        Thread.sleep(100);
        assertEquals(0, localGigaSpace1.count(null));
    }

    public void testListenerAsParameterTransactionRollback() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager1);
        final Listener listener = new Listener();
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AsyncFuture<Integer> value = localGigaSpace1.execute(new DelayedSimpleTask1(), 0, listener);
                status.setRollbackOnly();
            }
        });
        assertEquals(0, localGigaSpace1.count(null));
        while (listener.getResult() == null) {
            Thread.sleep(100);
        }
        // let it "commit"
        Thread.sleep(100);
        assertEquals(0, localGigaSpace1.count(null));
    }

    public void testListenerTransactionException() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager1);
        final ExceptionListener listener = new ExceptionListener();
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AsyncFuture<Integer> value = localGigaSpace1.execute(new DelayedSimpleTask1(), 0);
                value.setListener(listener);
                status.setRollbackOnly();
            }
        });
        assertEquals(0, localGigaSpace1.count(null));
        while (!listener.isCalled()) {
            Thread.sleep(100);
        }
        // let it "commit"
        Thread.sleep(100);
        assertEquals(0, localGigaSpace1.count(null));
    }

    public void testDistributedTransactionCommit() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(distTxManager);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AsyncFuture<Integer> value = distGigaSpace.execute(new SimpleDistributedTask1());
                try {
                    assertEquals(2, (int) value.get(1000, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
            }
        });
        assertEquals(4, distGigaSpace.count(null));
    }

    public void testDistributedTransactionRollback() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(distTxManager);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AsyncFuture<Integer> value = distGigaSpace.execute(new SimpleDistributedTask1());
                try {
                    assertEquals(2, (int) value.get(1000, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
                status.setRollbackOnly();
            }
        });
        assertEquals(0, distGigaSpace.count(null));
    }

    private static class Listener implements AsyncFutureListener<Integer> {

        private volatile AsyncResult<Integer> result;

        public void onResult(AsyncResult<Integer> result) {
            this.result = result;
        }

        public AsyncResult<Integer> getResult() {
            return result;
        }
    }

    private static class ExceptionListener implements AsyncFutureListener<Integer> {

        private volatile boolean called = false;

        public void onResult(AsyncResult<Integer> result) {
            called = true;
            throw new RuntimeException();
        }

        public boolean isCalled() {
            return called;
        }
    }

    @AutowireTask
    private static class SimpleDistributedTask1 implements DistributedTask<Integer, Integer> {

        private static final long serialVersionUID = -3310722770744601471L;

        @Resource(name = "gigaSpace1")
        transient GigaSpace gigaSpace1;

        @Resource(name = "gigaSpace2")
        transient GigaSpace gigaSpace2;

        public Integer execute() throws Exception {
            gigaSpace1.write(new Object());
            gigaSpace2.write(new Object());
            return 1;
        }

        public Integer reduce(List<AsyncResult<Integer>> asyncResults) throws Exception {
            int sum = 0;
            for (AsyncResult<Integer> result : asyncResults) {
                if (result.getException() != null) {
                    throw result.getException();
                }
                sum += result.getResult();
            }
            return sum;
        }
    }

    @AutowireTask
    private static class SimpleTask1 implements Task<Integer> {

        /**
         * 
         */
        private static final long serialVersionUID = -4297787552872006580L;
        @Resource(name = "gigaSpace1")
        transient GigaSpace gigaSpace;

        public Integer execute() throws Exception {
            gigaSpace.write(new Object());
            return 1;
        }
    }

    private static class SimpleTask2 implements Task<Integer> {

        /**
         * 
         */
        private static final long serialVersionUID = -7606733098742774260L;
        @TaskGigaSpace
        transient GigaSpace gigaSpace;

        public Integer execute() throws Exception {
            gigaSpace.write(new Object());
            return 1;
        }
    }

    @AutowireTask
    private static class DelayedSimpleTask1 implements Task<Integer> {

        private static final long serialVersionUID = 4699319020233503249L;

        @Resource(name = "gigaSpace1")
        transient GigaSpace gigaSpace;

        public Integer execute() throws Exception {
            gigaSpace.write(new Object());
            Thread.sleep(1000);
            return 1;
        }
    }
}