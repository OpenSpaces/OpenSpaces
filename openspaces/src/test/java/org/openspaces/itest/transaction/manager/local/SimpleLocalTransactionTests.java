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

package org.openspaces.itest.transaction.manager.local;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import net.jini.core.transaction.Transaction;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.AutowireTask;
import org.openspaces.core.executor.Task;
import org.openspaces.core.executor.TaskGigaSpace;
import org.openspaces.core.transaction.manager.ExistingJiniTransactionManager;
import org.openspaces.itest.utils.TestUtils;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author kimchy
 */
public class SimpleLocalTransactionTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace;      

    protected PlatformTransactionManager localTxManager;

    public SimpleLocalTransactionTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/transaction/manager/local/context.xml"};
    }

    protected void onSetUp() throws Exception {
        gigaSpace.clean();
    }

    protected void onTearDown() throws Exception {
        gigaSpace.clean();
    }

    public void testSimpleCommit() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new TestData1()));
                gigaSpace.write(new TestData1());
                assertNotNull(gigaSpace.read(new TestData1()));
            }
        });
        assertNotNull(gigaSpace.read(new TestData1()));
    }

    public void testSimpleRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new TestData1()));
                gigaSpace.write(new TestData1());
                assertNotNull(gigaSpace.read(new TestData1()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNull(gigaSpace.read(new TestData1()));
    }

    public void testTakeRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new TestData1()));
        gigaSpace.write(new TestData1());
        assertNotNull(gigaSpace.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNotNull(gigaSpace.take(new TestData1()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNotNull(gigaSpace.take(new TestData1()));
    }

    public void testPropogationRequiredWithCommit() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new TestData2()));
        assertNull(gigaSpace.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new TestData2()));
                assertNull(gigaSpace.read(new TestData1()));

                gigaSpace.write(new TestData2());

                assertNotNull(gigaSpace.read(new TestData2()));
                assertNull(gigaSpace.read(new TestData1()));

                TransactionTemplate innerTxTemplate = new TransactionTemplate(localTxManager);
                innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

                innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        gigaSpace.write(new TestData1());
                    }
                });

                assertNotNull(gigaSpace.read(new TestData1()));
            }
        });
        assertNotNull(gigaSpace.read(new TestData2()));
        assertNotNull(gigaSpace.read(new TestData1()));
    }

    public void testPropogationRequiredWithRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new TestData2()));
        assertNull(gigaSpace.read(new TestData1()));
        try {
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    assertNull(gigaSpace.read(new TestData2()));
                    assertNull(gigaSpace.read(new TestData1()));

                    gigaSpace.write(new TestData2());

                    assertNotNull(gigaSpace.read(new TestData2()));
                    assertNull(gigaSpace.read(new TestData1()));

                    TransactionTemplate innerTxTemplate = new TransactionTemplate(localTxManager);
                    innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

                    innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                        protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                            gigaSpace.write(new TestData1());
                            throw new RuntimeException();
                        }
                    });

                    assertNotNull(gigaSpace.read(new TestData1()));
                }
            });
        } catch (RuntimeException e) {
            // do nothing
        }
        assertNull(gigaSpace.read(new TestData1()));
        assertNull(gigaSpace.read(new TestData2()));
    }

    public void testPropogationRequiresNewWithCommit() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new TestData2()));
        assertNull(gigaSpace.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new TestData2()));
                assertNull(gigaSpace.read(new TestData1()));

                gigaSpace.write(new TestData2());

                assertNotNull(gigaSpace.read(new TestData2()));
                assertNull(gigaSpace.read(new TestData1()));

                TransactionTemplate innerTxTemplate = new TransactionTemplate(localTxManager);
                innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        gigaSpace.write(new TestData1());
                    }
                });

                assertNotNull(gigaSpace.read(new TestData1()));
            }
        });
        assertNotNull(gigaSpace.read(new TestData2()));
        assertNotNull(gigaSpace.read(new TestData1()));
    }

    public void testPropagationNotSupportedWithRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new TestData2()));
        assertNull(gigaSpace.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new TestData2()));
                assertNull(gigaSpace.read(new TestData1()));

                gigaSpace.write(new TestData2());

                assertNotNull(gigaSpace.read(new TestData2()));
                assertNull(gigaSpace.read(new TestData1()));

                TransactionTemplate innerTxTemplate = new TransactionTemplate(localTxManager);
                innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);

                innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        gigaSpace.write(new TestData1());
                    }
                });

                assertNotNull(gigaSpace.read(new TestData1()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNull(gigaSpace.read(new TestData2()));
        assertNotNull(gigaSpace.read(new TestData1()));
    }

    public void testPropogationRequiresNewWithRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new TestData2()));
        assertNull(gigaSpace.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new TestData2()));
                assertNull(gigaSpace.read(new TestData1()));

                gigaSpace.write(new TestData2());

                assertNotNull(gigaSpace.read(new TestData2()));
                assertNull(gigaSpace.read(new TestData1()));

                TransactionTemplate innerTxTemplate = new TransactionTemplate(localTxManager);
                innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        gigaSpace.write(new TestData1());
                        transactionStatus.setRollbackOnly();
                    }
                });

                assertNull(gigaSpace.read(new TestData1()));
            }
        });
        assertNotNull(gigaSpace.read(new TestData2()));
        assertNull(gigaSpace.read(new TestData1()));
    }

    public void testSimpleExistingTransactionWithCommit() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new TestData2()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new TestData2()));
                gigaSpace.write(new TestData2());
                assertNotNull(gigaSpace.read(new TestData2()));

                final Transaction tx = gigaSpace.getCurrentTransaction();

                final AtomicReference<AssertionFailedError> exceptionHolder = new AtomicReference<AssertionFailedError>();
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        ExistingJiniTransactionManager.bindExistingTransaction(tx);
                        try {
                            assertNotNull(gigaSpace.getCurrentTransaction());
                            assertSame(tx, gigaSpace.getCurrentTransaction());

                            TransactionTemplate innerTxTemplate = new TransactionTemplate(localTxManager);
                            innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

                            innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                                    assertNotNull(gigaSpace.getCurrentTransaction());
                                    assertSame(tx, gigaSpace.getCurrentTransaction());
                                    gigaSpace.write(new TestData1());
                                }
                            });

                            assertNotNull(gigaSpace.read(new TestData1()));


                        } catch (AssertionFailedError e) {
                            exceptionHolder.set(e);
                        }
                    }
                });
                thread.start();
                try {
                    thread.join(1000);
                } catch (InterruptedException e) {
                    // do nothing
                }
                if (exceptionHolder.get() != null) {
                    throw exceptionHolder.get();
                }
            }
        });
        assertNotNull(gigaSpace.read(new TestData2()));
        assertNotNull(gigaSpace.read(new TestData1()));
    }

    public void testPropagationNotSupportedWithRollback2() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new TestData2()));
        assertNull(gigaSpace.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new TestData2()));
                assertNull(gigaSpace.read(new TestData1()));

                gigaSpace.write(new TestData2());

                assertNotNull(gigaSpace.read(new TestData2()));
                assertNull(gigaSpace.read(new TestData1()));

                TransactionTemplate innerTxTemplate = new TransactionTemplate(localTxManager);
                innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);

                innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        gigaSpace.write(new TestData1());
                    }
                });

                assertNotNull(gigaSpace.read(new TestData1()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNull(gigaSpace.read(new TestData2()));
        assertNotNull(gigaSpace.read(new TestData1()));
    }

    public void testPropagationNotSupportedWithRollbackTask() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        assertNull(gigaSpace.read(new TestData2()));
        assertNull(gigaSpace.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new TestData2()));
                assertNull(gigaSpace.read(new TestData1()));

                gigaSpace.write(new TestData2());

                assertNotNull(gigaSpace.read(new TestData2()));
                assertNull(gigaSpace.read(new TestData1()));
                Future<Integer> future = gigaSpace.execute(new SimpleTask1());
                try {
                    Assert.assertEquals((Integer) 1, future.get());
                } catch (Exception e) {
                   e.printStackTrace();
                }

                TestUtils.repetitive(new Runnable() {
                    @Override
                    public void run() {
                        assertNotNull(gigaSpace.read(new TestData1()));
                    }
                }, 1000);
                transactionStatus.setRollbackOnly();
            }
        });
        assertNull(gigaSpace.read(new TestData2()));
        assertNotNull(gigaSpace.read(new TestData1()));
    }


    @AutowireTask
    private class SimpleTask1 implements Task<Integer> {
        private static final long serialVersionUID = -4297787552872006580L;

        @TaskGigaSpace
        transient GigaSpace gigaSpace;

        public Integer execute() throws Exception {
            TransactionTemplate innerTxTemplate = new TransactionTemplate(localTxManager);
            innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
            innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    gigaSpace.write(new TestData1());
                    assertNotNull(gigaSpace.read(new TestData1()));
                }
            });

            return 1;
        }
    }
}
