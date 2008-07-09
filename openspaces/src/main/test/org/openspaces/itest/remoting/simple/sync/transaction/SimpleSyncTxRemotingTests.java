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

package org.openspaces.itest.remoting.simple.sync.transaction;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author kimchy
 */
public class SimpleSyncTxRemotingTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace;

    protected SimpleTxService simpleService;

    protected DefaultSimpleTxService defaultService;

    protected PlatformTransactionManager localTxManager;

    public SimpleSyncTxRemotingTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/remoting/simple/sync/transaction/simple-sync-tx-remoting.xml"};
    }

    protected void onSetUp() throws Exception {
        gigaSpace.clear(new Object());
    }

    public void testUnderTransactionWithCommit() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);

        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                simpleService.bahh(new TestMessage("test1"));
            }
        });
        assertNotNull(gigaSpace.read(new TestMessage()));
        assertTrue(defaultService.isTransactional());
    }

    public void testUnderTransactionWithCommitAndExceptionByTheService() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);

        txTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    simpleService.bahh(new TestMessage("throwme"));
                    fail();
                } catch (RuntimeException e) {
                    // all is well, don't propagagte so we won't rollback
                }
            }
        });
        assertNotNull(gigaSpace.read(new TestMessage()));
        assertTrue(defaultService.isTransactional());
    }

    public void testUnderTransactionWithRollbackPropogatedException() {
        TransactionTemplate txTemplate = new TransactionTemplate(localTxManager);

        try {
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    simpleService.bahh(new TestMessage("throwme"));
                }
            });
            fail();
        } catch (RuntimeException e) {
            //all is well, we throw an exception
        }
        assertNull(gigaSpace.read(new TestMessage()));
        assertTrue(defaultService.isTransactional());
    }

    public void testNoTransaction() {
        simpleService.bahh(new TestMessage("test1"));
        assertNotNull(gigaSpace.read(new TestMessage()));
        assertFalse(defaultService.isTransactional());
    }
}