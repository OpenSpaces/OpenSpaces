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
public class OpenSpacesFlowInOnlyTests extends OpenSpacesFlowAbstractTest {
    
    /**
     * Test case sending non clustered inonly message.
     *
     * @throws Exception
     */
    public void test() throws Exception {
        SenderComponent sender = new SenderComponent();
        ReceiverComponent receiver = new ReceiverComponent();

        sender.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));
        senderContainer.activateComponent(new ActivationSpec("sender", sender));
        receiverContainer.activateComponent(new ActivationSpec("receiver", receiver));
        Thread.sleep(1000);

        sender.sendMessages(NUM_MESSAGES);
        Thread.sleep(3000);
        receiver.getMessageList().assertMessagesReceived(NUM_MESSAGES);
    }
}
