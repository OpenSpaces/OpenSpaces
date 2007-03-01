package org.openspaces.itest.transaction.manager.local;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
        assertNull(gigaSpace.read(new Object()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new Object()));
                gigaSpace.write(new Object());
                assertNotNull(gigaSpace.read(new Object()));
            }
        });
        assertNotNull(gigaSpace.read(new Object()));
    }

    public void testSimpleRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new Object()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new Object()));
                gigaSpace.write(new Object());
                assertNotNull(gigaSpace.read(new Object()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNull(gigaSpace.read(new Object()));
    }

    public void testTakeRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new Object()));
        gigaSpace.write(new Object());
        assertNotNull(gigaSpace.read(new Object()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNotNull(gigaSpace.take(new Object()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNotNull(gigaSpace.take(new Object()));
    }

    public void testPropogationRequiredWithCommit() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new Object()));
        assertNull(gigaSpace.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new Object()));
                assertNull(gigaSpace.read(new TestData1()));

                gigaSpace.write(new Object());

                assertNotNull(gigaSpace.read(new Object()));
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
        assertNotNull(gigaSpace.read(new Object()));
        assertNotNull(gigaSpace.read(new TestData1()));
    }

    public void testPropogationRequiredWithRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new Object()));
        assertNull(gigaSpace.read(new TestData1()));
        try {
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    assertNull(gigaSpace.read(new Object()));
                    assertNull(gigaSpace.read(new TestData1()));

                    gigaSpace.write(new Object());

                    assertNotNull(gigaSpace.read(new Object()));
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
        assertNull(gigaSpace.read(new Object()));
    }

    public void testPropogationRequiresNewWithCommit() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new Object()));
        assertNull(gigaSpace.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new Object()));
                assertNull(gigaSpace.read(new TestData1()));

                gigaSpace.write(new Object());

                assertNotNull(gigaSpace.read(new Object()));
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
        assertNotNull(gigaSpace.read(new Object()));
        assertNotNull(gigaSpace.read(new TestData1()));
    }

    public void testPropogationRequiresNewWithRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);
        assertNull(gigaSpace.read(new Object()));
        assertNull(gigaSpace.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace.read(new Object()));
                assertNull(gigaSpace.read(new TestData1()));

                gigaSpace.write(new Object());

                assertNotNull(gigaSpace.read(new Object()));
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
        assertNotNull(gigaSpace.read(new Object()));
        assertNull(gigaSpace.read(new TestData1()));
    }
}
