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

package org.openspaces.itest.esb.mule.eventcontainer.tx;

import org.mule.api.config.ConfigurationException;
import org.openspaces.itest.esb.mule.AbstractMuleTests;
import org.openspaces.itest.esb.mule.SimpleMessage;

/**
 * @author kimchy
 */
public class TxRollbackEventContainerTests extends AbstractMuleTests {

    public void testTxRollbackLocalTransaction() throws ConfigurationException {
        int numberOfMsgs = 1;
        for (int i = 0; i < numberOfMsgs; i++) {
            SimpleMessage message = new SimpleMessage("Hello World " + i, false);
            gigaSpace.write(message);
        }

        //blocking wait untill the mule writes back the messages to the space after reading them.
        for (int i = 0; i < numberOfMsgs; i++) {
            SimpleMessage template = new SimpleMessage(true);
            SimpleMessage message = gigaSpace.take(template, TIMEOUT);
            assertNotNull("should receive a response here...", message);
            assertEquals("rolledback", message.getMessage());
        }
        assertEquals(0, gigaSpace.count(new SimpleMessage()));
    }

    @Override
    protected String getConfigResources() {
        return "org/openspaces/itest/esb/mule/eventcontainer/tx/local-tx.xml";
    }
}