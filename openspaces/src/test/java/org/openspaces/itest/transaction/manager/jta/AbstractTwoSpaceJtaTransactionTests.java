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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.openspaces.itest.utils.EmptySpaceDataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractTwoSpaceJtaTransactionTests   { 

     @Autowired protected GigaSpace gigaSpace1;

     @Autowired protected GigaSpace gigaSpace2;

     @Autowired protected PlatformTransactionManager transactionManager;

    public AbstractTwoSpaceJtaTransactionTests() {
 
    }

     @Before public  void onSetUp() throws Exception {
        gigaSpace1.clear(null);
        gigaSpace2.clear(null);
    }

     @After public  void onTearDown() throws Exception {
        gigaSpace1.clear(null);
        gigaSpace2.clear(null);
    }

     @Test public void testSimpleCommit() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
                gigaSpace1.write(new EmptySpaceDataObject());
                assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
            }
        });
        assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
    }

     @Test public void testSimpleCommitTwoSpaces() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace2.read(new EmptySpaceDataObject()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
                assertNull(gigaSpace2.read(new EmptySpaceDataObject()));
                gigaSpace1.write(new EmptySpaceDataObject());
                gigaSpace2.write(new EmptySpaceDataObject());
                assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
                assertNotNull(gigaSpace2.read(new EmptySpaceDataObject()));
            }
        });
        assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNotNull(gigaSpace2.read(new EmptySpaceDataObject()));
    }

     @Test public void testSimpleRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
                gigaSpace1.write(new EmptySpaceDataObject());
                assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
    }

     @Test public void testSimpleRollbackTwoSpaces() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
                gigaSpace1.write(new EmptySpaceDataObject());
                assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
    }

     @Test public void testTakeRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        gigaSpace1.write(new EmptySpaceDataObject());
        assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNotNull(gigaSpace1.take(new EmptySpaceDataObject()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNotNull(gigaSpace1.take(new EmptySpaceDataObject()));
    }

     @Test public void testTakeRollbackTwoSpaces() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace2.read(new EmptySpaceDataObject()));
        gigaSpace1.write(new EmptySpaceDataObject());
        gigaSpace2.write(new EmptySpaceDataObject());
        assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNotNull(gigaSpace2.read(new EmptySpaceDataObject()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNotNull(gigaSpace1.take(new EmptySpaceDataObject()));
                assertNotNull(gigaSpace2.take(new EmptySpaceDataObject()));
                transactionStatus.setRollbackOnly();
            }
        });
        assertNotNull(gigaSpace1.take(new EmptySpaceDataObject()));
        assertNotNull(gigaSpace2.take(new EmptySpaceDataObject()));
    }

     @Test public void testPropogationRequiredWithCommit() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace1.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
                assertNull(gigaSpace1.read(new TestData1()));

                gigaSpace1.write(new EmptySpaceDataObject());

                assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
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
        assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNotNull(gigaSpace1.read(new TestData1()));
    }

     @Test public void testPropogationRequiredWithCommitTwoSpaces() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace2.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace1.read(new TestData1()));
        assertNull(gigaSpace2.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
                assertNull(gigaSpace2.read(new EmptySpaceDataObject()));
                assertNull(gigaSpace1.read(new TestData1()));
                assertNull(gigaSpace2.read(new TestData1()));

                gigaSpace1.write(new EmptySpaceDataObject());
                gigaSpace2.write(new EmptySpaceDataObject());

                assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
                assertNotNull(gigaSpace2.read(new EmptySpaceDataObject()));
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
        assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNotNull(gigaSpace2.read(new EmptySpaceDataObject()));
        assertNotNull(gigaSpace1.read(new TestData1()));
        assertNotNull(gigaSpace2.read(new TestData1()));
    }

     @Test public void testPropogationRequiredWithRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace2.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace1.read(new TestData1()));
        assertNull(gigaSpace2.read(new TestData1()));
        try {
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
                    assertNull(gigaSpace2.read(new EmptySpaceDataObject()));
                    assertNull(gigaSpace1.read(new TestData1()));
                    assertNull(gigaSpace2.read(new TestData1()));

                    gigaSpace1.write(new EmptySpaceDataObject());
                    gigaSpace2.write(new EmptySpaceDataObject());

                    assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
                    assertNotNull(gigaSpace2.read(new EmptySpaceDataObject()));
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
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace2.read(new EmptySpaceDataObject()));
    }

     @Test public void testPropogationRequiresNewWithCommit() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace1.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
                assertNull(gigaSpace1.read(new TestData1()));

                gigaSpace1.write(new EmptySpaceDataObject());

                assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
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
        assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNotNull(gigaSpace1.read(new TestData1()));
    }

     @Test public void testPropogationRequiresNewWithCommitWithTwoSpaces() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace2.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace1.read(new TestData1()));
        assertNull(gigaSpace2.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
                assertNull(gigaSpace2.read(new EmptySpaceDataObject()));
                assertNull(gigaSpace1.read(new TestData1()));
                assertNull(gigaSpace2.read(new TestData1()));

                gigaSpace1.write(new EmptySpaceDataObject());
                gigaSpace2.write(new EmptySpaceDataObject());

                assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
                assertNotNull(gigaSpace2.read(new EmptySpaceDataObject()));
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
        assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNotNull(gigaSpace2.read(new EmptySpaceDataObject()));
        assertNotNull(gigaSpace1.read(new TestData1()));
        assertNotNull(gigaSpace2.read(new TestData1()));
    }

     @Test public void testPropogationRequiresNewWithRollback() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace1.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
                assertNull(gigaSpace1.read(new TestData1()));

                gigaSpace1.write(new EmptySpaceDataObject());

                assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
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
        assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace1.read(new TestData1()));
    }

     @Test public void testPropogationRequiresNewWithRollbackWithTwoSpaces() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace2.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace1.read(new TestData1()));
        assertNull(gigaSpace2.read(new TestData1()));
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                assertNull(gigaSpace1.read(new EmptySpaceDataObject()));
                assertNull(gigaSpace2.read(new EmptySpaceDataObject()));
                assertNull(gigaSpace1.read(new TestData1()));
                assertNull(gigaSpace2.read(new TestData1()));

                gigaSpace1.write(new EmptySpaceDataObject());
                gigaSpace2.write(new EmptySpaceDataObject());

                assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
                assertNotNull(gigaSpace2.read(new EmptySpaceDataObject()));
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
        assertNotNull(gigaSpace1.read(new EmptySpaceDataObject()));
        assertNotNull(gigaSpace2.read(new EmptySpaceDataObject()));
        assertNull(gigaSpace1.read(new TestData1()));
        assertNull(gigaSpace2.read(new TestData1()));
    }

     @Test public void testPropagationNotSupportedWithRollback() {
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
    
//     @Test public void testPropagationNotSupportedWithRollbackTask() {
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

