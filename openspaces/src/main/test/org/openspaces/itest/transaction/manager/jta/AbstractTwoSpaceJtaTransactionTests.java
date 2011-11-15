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

package org.openspaces.itest.transaction.manager.jta;

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
public abstract class AbstractTwoSpaceJtaTransactionTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace1;

    protected GigaSpace gigaSpace2;

    protected PlatformTransactionManager transactionManager;

    public AbstractTwoSpaceJtaTransactionTests() {
        setPopulateProtectedVariables(true);
    }

    protected void onSetUp() throws Exception {
        gigaSpace1.clean();
        gigaSpace2.clean();
    }

    protected void onTearDown() throws Exception {
        gigaSpace1.clean();
        gigaSpace2.clean();
    }

    public void testSimpleCommit() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new Object()));
                gigaSpace1.write(new Object());
                assertNotNull(gigaSpace1.read(new Object()));
            }
        });
        assertNotNull(gigaSpace1.read(new Object()));
    }

    public void testSimpleCommitTwoSpaces() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        assertNull(gigaSpace2.read(new Object()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new Object()));
                assertNull(gigaSpace2.read(new Object()));
                gigaSpace1.write(new Object());
                gigaSpace2.write(new Object());
                assertNotNull(gigaSpace1.read(new Object()));
                assertNotNull(gigaSpace2.read(new Object()));
            }
        });
        assertNotNull(gigaSpace1.read(new Object()));
        assertNotNull(gigaSpace2.read(new Object()));
    }

    public void testSimpleRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new Object()));
                gigaSpace1.write(new Object());
                assertNotNull(gigaSpace1.read(new Object()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNull(gigaSpace1.read(new Object()));
    }

    public void testSimpleRollbackTwoSpaces() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new Object()));
                gigaSpace1.write(new Object());
                assertNotNull(gigaSpace1.read(new Object()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNull(gigaSpace1.read(new Object()));
    }

    public void testTakeRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        gigaSpace1.write(new Object());
        assertNotNull(gigaSpace1.read(new Object()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNotNull(gigaSpace1.take(new Object()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNotNull(gigaSpace1.take(new Object()));
    }

    public void testTakeRollbackTwoSpaces() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        assertNull(gigaSpace2.read(new Object()));
        gigaSpace1.write(new Object());
        gigaSpace2.write(new Object());
        assertNotNull(gigaSpace1.read(new Object()));
        assertNotNull(gigaSpace2.read(new Object()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNotNull(gigaSpace1.take(new Object()));
                assertNotNull(gigaSpace2.take(new Object()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNotNull(gigaSpace1.take(new Object()));
        assertNotNull(gigaSpace2.take(new Object()));
    }

    public void testPropogationRequiredWithCommit() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        assertNull(gigaSpace1.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new Object()));
                assertNull(gigaSpace1.read(new TestData1()));

                gigaSpace1.write(new Object());

                assertNotNull(gigaSpace1.read(new Object()));
                assertNull(gigaSpace1.read(new TestData1()));

                TransactionTemplate innerTxTemplate = new TransactionTemplate(transactionManager);
                innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

                innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        gigaSpace1.write(new TestData1());
                    }
                });

                assertNotNull(gigaSpace1.read(new TestData1()));
            }
        });
        assertNotNull(gigaSpace1.read(new Object()));
        assertNotNull(gigaSpace1.read(new TestData1()));
    }

    public void testPropogationRequiredWithCommitTwoSpaces() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        assertNull(gigaSpace2.read(new Object()));
        assertNull(gigaSpace1.read(new TestData1()));
        assertNull(gigaSpace2.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new Object()));
                assertNull(gigaSpace2.read(new Object()));
                assertNull(gigaSpace1.read(new TestData1()));
                assertNull(gigaSpace2.read(new TestData1()));

                gigaSpace1.write(new Object());
                gigaSpace2.write(new Object());

                assertNotNull(gigaSpace1.read(new Object()));
                assertNotNull(gigaSpace2.read(new Object()));
                assertNull(gigaSpace1.read(new TestData1()));
                assertNull(gigaSpace2.read(new TestData1()));

                TransactionTemplate innerTxTemplate = new TransactionTemplate(transactionManager);
                innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

                innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        gigaSpace1.write(new TestData1());
                        gigaSpace2.write(new TestData1());
                    }
                });

                assertNotNull(gigaSpace1.read(new TestData1()));
                assertNotNull(gigaSpace2.read(new TestData1()));
            }
        });
        assertNotNull(gigaSpace1.read(new Object()));
        assertNotNull(gigaSpace2.read(new Object()));
        assertNotNull(gigaSpace1.read(new TestData1()));
        assertNotNull(gigaSpace2.read(new TestData1()));
    }

    public void testPropogationRequiredWithRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        assertNull(gigaSpace2.read(new Object()));
        assertNull(gigaSpace1.read(new TestData1()));
        assertNull(gigaSpace2.read(new TestData1()));
        try {
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    assertNull(gigaSpace1.read(new Object()));
                    assertNull(gigaSpace2.read(new Object()));
                    assertNull(gigaSpace1.read(new TestData1()));
                    assertNull(gigaSpace2.read(new TestData1()));

                    gigaSpace1.write(new Object());
                    gigaSpace2.write(new Object());

                    assertNotNull(gigaSpace1.read(new Object()));
                    assertNotNull(gigaSpace2.read(new Object()));
                    assertNull(gigaSpace1.read(new TestData1()));
                    assertNull(gigaSpace2.read(new TestData1()));

                    TransactionTemplate innerTxTemplate = new TransactionTemplate(transactionManager);
                    innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

                    innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                        protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                            gigaSpace1.write(new TestData1());
                            gigaSpace2.write(new TestData1());
                            throw new RuntimeException();
                        }
                    });

                    assertNotNull(gigaSpace1.read(new TestData1()));
                    assertNotNull(gigaSpace2.read(new TestData1()));
                }
            });
        } catch (RuntimeException e) {
            // do nothing
        }
        assertNull(gigaSpace1.read(new TestData1()));
        assertNull(gigaSpace2.read(new TestData1()));
        assertNull(gigaSpace1.read(new Object()));
        assertNull(gigaSpace2.read(new Object()));
    }

    public void testPropogationRequiresNewWithCommit() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        assertNull(gigaSpace1.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new Object()));
                assertNull(gigaSpace1.read(new TestData1()));

                gigaSpace1.write(new Object());

                assertNotNull(gigaSpace1.read(new Object()));
                assertNull(gigaSpace1.read(new TestData1()));

                TransactionTemplate innerTxTemplate = new TransactionTemplate(transactionManager);
                innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        gigaSpace1.write(new TestData1());
                    }
                });

                assertNotNull(gigaSpace1.read(new TestData1()));
            }
        });
        assertNotNull(gigaSpace1.read(new Object()));
        assertNotNull(gigaSpace1.read(new TestData1()));
    }

    public void testPropogationRequiresNewWithCommitWithTwoSpaces() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        assertNull(gigaSpace2.read(new Object()));
        assertNull(gigaSpace1.read(new TestData1()));
        assertNull(gigaSpace2.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new Object()));
                assertNull(gigaSpace2.read(new Object()));
                assertNull(gigaSpace1.read(new TestData1()));
                assertNull(gigaSpace2.read(new TestData1()));

                gigaSpace1.write(new Object());
                gigaSpace2.write(new Object());

                assertNotNull(gigaSpace1.read(new Object()));
                assertNotNull(gigaSpace2.read(new Object()));
                assertNull(gigaSpace1.read(new TestData1()));
                assertNull(gigaSpace2.read(new TestData1()));

                TransactionTemplate innerTxTemplate = new TransactionTemplate(transactionManager);
                innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        gigaSpace1.write(new TestData1());
                        gigaSpace2.write(new TestData1());
                    }
                });

                assertNotNull(gigaSpace1.read(new TestData1()));
                assertNotNull(gigaSpace2.read(new TestData1()));
            }
        });
        assertNotNull(gigaSpace1.read(new Object()));
        assertNotNull(gigaSpace2.read(new Object()));
        assertNotNull(gigaSpace1.read(new TestData1()));
        assertNotNull(gigaSpace2.read(new TestData1()));
    }

    public void testPropogationRequiresNewWithRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        assertNull(gigaSpace1.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new Object()));
                assertNull(gigaSpace1.read(new TestData1()));

                gigaSpace1.write(new Object());

                assertNotNull(gigaSpace1.read(new Object()));
                assertNull(gigaSpace1.read(new TestData1()));

                TransactionTemplate innerTxTemplate = new TransactionTemplate(transactionManager);
                innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        gigaSpace1.write(new TestData1());
                        transactionStatus.setRollbackOnly();
                    }
                });

                assertNull(gigaSpace1.read(new TestData1()));
            }
        });
        assertNotNull(gigaSpace1.read(new Object()));
        assertNull(gigaSpace1.read(new TestData1()));
    }

    public void testPropogationRequiresNewWithRollbackWithTwoSpaces() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new Object()));
        assertNull(gigaSpace2.read(new Object()));
        assertNull(gigaSpace1.read(new TestData1()));
        assertNull(gigaSpace2.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new Object()));
                assertNull(gigaSpace2.read(new Object()));
                assertNull(gigaSpace1.read(new TestData1()));
                assertNull(gigaSpace2.read(new TestData1()));

                gigaSpace1.write(new Object());
                gigaSpace2.write(new Object());

                assertNotNull(gigaSpace1.read(new Object()));
                assertNotNull(gigaSpace2.read(new Object()));
                assertNull(gigaSpace1.read(new TestData1()));
                assertNull(gigaSpace2.read(new TestData1()));

                TransactionTemplate innerTxTemplate = new TransactionTemplate(transactionManager);
                innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        gigaSpace1.write(new TestData1());
                        gigaSpace2.write(new TestData1());
                        transactionStatus.setRollbackOnly();
                    }
                });

                assertNull(gigaSpace1.read(new TestData1()));
                assertNull(gigaSpace2.read(new TestData1()));
            }
        });
        assertNotNull(gigaSpace1.read(new Object()));
        assertNotNull(gigaSpace2.read(new Object()));
        assertNull(gigaSpace1.read(new TestData1()));
        assertNull(gigaSpace2.read(new TestData1()));
    }

    public void testPropagationNotSupportedWithRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        assertNull(gigaSpace1.read(new TestData2()));
        assertNull(gigaSpace1.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new TestData2()));
                assertNull(gigaSpace1.read(new TestData1()));

                gigaSpace1.write(new TestData2());

                assertNotNull(gigaSpace1.read(new TestData2()));
                assertNull(gigaSpace1.read(new TestData1()));

                TransactionTemplate innerTxTemplate = new TransactionTemplate(transactionManager);
                innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);

                innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        gigaSpace1.write(new TestData1());
                    }
                });

                assertNotNull(gigaSpace1.read(new TestData1()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNull(gigaSpace1.read(new TestData2()));
        assertNotNull(gigaSpace1.read(new TestData1()));
    }

//    public void testPropagationNotSupportedWithRollbackTask() {
//        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
//        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        assertNull(gigaSpace1.read(new TestData2()));
//        assertNull(gigaSpace1.read(new TestData1()));
//        txTemplate.execute(new TransactionCallbackWithoutResult() {
//            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
//                assertNull(gigaSpace1.read(new TestData2()));
//                assertNull(gigaSpace1.read(new TestData1()));
//
//                gigaSpace1.write(new TestData2());
//
//                assertNotNull(gigaSpace1.read(new TestData2()));
//                assertNull(gigaSpace1.read(new TestData1()));
//                Future<Integer> future = gigaSpace1.execute(new SimpleTask1());
//                try {
//                    Assert.assertEquals((Integer) 1, future.get());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                TestUtils.repetitive(new Runnable() {
//                    @Override
//                    public void run() {
//                        assertNotNull(gigaSpace1.read(new TestData1()));
//                    }
//                }, 1000);
//                 transactionStatus.setRollbackOnly();
//            }
//        });
//        assertNull(gigaSpace1.read(new TestData2()));
//        assertNotNull(gigaSpace1.read(new TestData1()));
//    }
//
//
//    @AutowireTask
//    private class SimpleTask1 implements Task<Integer> {
//        private static final long serialVersionUID = -4297787552872006580L;
//
//        @TaskGigaSpace
//        transient GigaSpace gigaSpace;
//
//        public Integer execute() throws Exception {
//            TransactionTemplate innerTxTemplate = new TransactionTemplate(transactionManager);
//            innerTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
//            innerTxTemplate.execute(new TransactionCallbackWithoutResult() {
//                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
//                    gigaSpace.write(new TestData1());
//                    assertNotNull(gigaSpace.read(new TestData1()));
//                }
//            });
//            return 1;
//        }
//
//    }


}