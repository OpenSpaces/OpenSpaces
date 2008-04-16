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
package org.openspaces.itest.esb.servicemix.flow;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;

/**
 * @author yitzhaki
 */
public class OpenSpacesFlowClusteredInOnlyTests extends OpenSpacesFlowAbstractTest {

    /**
     * Test case sending clustered inonly message.
     *
     * @throws Exception
     */
    public void test() throws Exception {
        final SenderComponent sender2 = new SenderComponent();
        final ReceiverComponent receiver1 = new ReceiverComponent();
        final ReceiverComponent receiver2 = new ReceiverComponent();
        sender2.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));

        senderContainer.activateComponent(new ActivationSpec("sender", sender2));
        senderContainer.activateComponent(new ActivationSpec("receiver", receiver1));
        receiverContainer.activateComponent(new ActivationSpec("receiver", receiver2));
        Thread.sleep(2000);

        sender2.sendMessages(NUM_MESSAGES);
        Thread.sleep(5000);

        assertTrue(receiver1.getMessageList().hasReceivedMessage());
        assertTrue(receiver2.getMessageList().hasReceivedMessage());
        receiver1.getMessageList().flushMessages();
        receiver2.getMessageList().flushMessages();

        senderContainer.deactivateComponent("receiver");
        Thread.sleep(2000);

        sender2.sendMessages(NUM_MESSAGES);
        Thread.sleep(4000);
        assertFalse(receiver1.getMessageList().hasReceivedMessage());
        assertTrue(receiver2.getMessageList().hasReceivedMessage());
        receiver1.getMessageList().flushMessages();
        receiver2.getMessageList().flushMessages();

        senderContainer.activateComponent(new ActivationSpec("receiver", receiver1));
        receiverContainer.deactivateComponent("receiver");
        Thread.sleep(2000);

        sender2.sendMessages(NUM_MESSAGES);
        Thread.sleep(4000);
        assertTrue(receiver1.getMessageList().hasReceivedMessage());
        assertFalse(receiver2.getMessageList().hasReceivedMessage());
        receiver1.getMessageList().flushMessages();
        receiver2.getMessageList().flushMessages();
    }
}
