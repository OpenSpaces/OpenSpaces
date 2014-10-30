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

package org.openspaces.itest.remoting.simple.transactionalservice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/remoting/simple/transactionalservice/transactional-service.xml")
public class SimpleTransactionalServiceTests   { 

     @Autowired protected GigaSpace gigaSpace;

     @Autowired protected SimpleTxService simpleService;

     @Autowired protected PlatformTransactionManager localTxManager;

    public SimpleTransactionalServiceTests() {
 
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/remoting/simple/transactionalservice/transactional-service.xml"};
    }

     @Before public  void onSetUp() throws Exception {
        gigaSpace.clear(new Object());
    }

     @Test public void testRollbackTransaction() {
        try {
            simpleService.bahh(new TestMessage("throwme"));
            fail();
        } catch (RuntimeException e) {
            // all is well
        }
        assertNull(gigaSpace.read(new TestMessage()));
    }
}

