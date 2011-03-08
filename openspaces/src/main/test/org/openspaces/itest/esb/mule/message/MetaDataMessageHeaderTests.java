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

package org.openspaces.itest.esb.mule.message;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.config.ConfigurationException;
import org.openspaces.itest.esb.mule.AbstractMuleTests;

/**
 * Tests mule connector, receive and process single object at a time.
 *
 * This test write MessageWithMessageHeader without metadata to the space.
 * The Appliction reads the <code>org.openspaces.itest.esb.mule.message.MessageWithMessageHeader</code> from the space and for
 * each MessageWithMessageHeader it change it name, creates <code> ProcessedMessage </code> object and copy the metadata
 * to it, then write it back to the space.
 *
 * @author yitzhaki
 */
public class MetaDataMessageHeaderTests extends AbstractMuleTests {

    public void testTakeSingleFromSpace() throws ConfigurationException {
        int numberOfMsgs = 10;
        List<MessageWithMessageHeader> list = new ArrayList<MessageWithMessageHeader>(numberOfMsgs);

        for (int i = 0; i < numberOfMsgs; i++) {
            MessageWithMessageHeader message = new MessageWithMessageHeader("Hello World " + i, i + "");
            message.setRead(false);
            message.setCorrelationSequence(i);
            message.setCorrelationId("CorrelationId " + i);
            message.setCorrelationGroupSize(i);
            message.setProperty("name", "name " + i);
            gigaSpace.write(message);
            list.add(message);
        }

        //blocking wait untill the mule writes back the messages to the space after reading them.
        for (int i = 0; i < numberOfMsgs; i++) {
            String msg = "Hello World " + i;
            MessageWithMessageHeader template = new MessageWithMessageHeader(msg, i + "");
            template.setRead(true);
            MessageWithMessageHeader message = gigaSpace.take(template, TIMEOUT);
            assertEquals("Hello World " + i, message.getMessage());
            assertEquals(i + "", message.getUniqueId());
            assertEquals(new Integer(i), message.getCorrelationGroupSize());
            assertEquals(new Integer(i), message.getCorrelationSequence());
            assertEquals("new name " + i, message.getProperty("name"));
        }
        assertEquals(0, gigaSpace.count(new MessageWithMessageHeader()));
    }

    @Override
    protected String getConfigResources() {
        return "org/openspaces/itest/esb/mule/message/metadatamessageheader.xml";
    }
}