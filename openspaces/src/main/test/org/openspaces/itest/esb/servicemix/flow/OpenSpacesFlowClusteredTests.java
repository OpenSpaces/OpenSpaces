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

import junit.framework.TestCase;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.esb.servicemix.flow.OpenSpacesFlow;

/**
 * Test several ways sending messages within clustered flows.
 *
 * @author yitzhaki
 */
public class OpenSpacesFlowClusteredTests extends TestCase {

    private static final int NUM_MESSAGES = 10;

    protected JBIContainer senderContainer = new JBIContainer();
    protected JBIContainer receiverContainer = new JBIContainer();


    /*
    * @see TestCase#setUp()
    */
    protected void setUp() throws Exception {
        super.setUp();

        GigaSpace gigaSpace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("/./space").
                lookupGroups(System.getProperty("user.name")).space()).gigaSpace();
        OpenSpacesFlow osflow = new OpenSpacesFlow();
        osflow.setGigaSpace(gigaSpace);

        senderContainer.setName("senderContainer");
        senderContainer.setFlowName("openspaces");
        senderContainer.setFlow(osflow);
        senderContainer.init();
        senderContainer.start();
        Object senderFlow = senderContainer.getFlow();
        assertTrue(senderFlow instanceof OpenSpacesFlow);


        osflow = new OpenSpacesFlow();
        osflow.setGigaSpace(gigaSpace);
        receiverContainer.setName("receiverContainer");
        receiverContainer.setFlowName("openspaces");
        receiverContainer.setFlow(osflow);
        receiverContainer.init();
        receiverContainer.start();
        Object receiverFlow = receiverContainer.getFlow();
        assertTrue(receiverFlow instanceof OpenSpacesFlow);
        Thread.sleep(2000);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        senderContainer.shutDown();
        receiverContainer.shutDown();
    }

    /**
     * Test case sending non clustered inonly message.
     *
     * @throws Exception
     */
    public void testInOnly() throws Exception {
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

    /**
     * Test case sending clustered inonly message.
     *
     * @throws Exception
     */
    public void testClusteredInOnly() throws Exception {
        final SenderComponent sender2 = new SenderComponent();
        final ReceiverComponent receiver1 = new ReceiverComponent();
        final ReceiverComponent receiver2 = new ReceiverComponent();
        sender2.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));

        senderContainer.activateComponent(new ActivationSpec("sender", sender2));
        senderContainer.activateComponent(new ActivationSpec("receiver", receiver1));
        receiverContainer.activateComponent(new ActivationSpec("receiver", receiver2));
        Thread.sleep(1000);

        sender2.sendMessages(NUM_MESSAGES);
        Thread.sleep(3000);

        assertTrue(receiver1.getMessageList().hasReceivedMessage());
        assertTrue(receiver2.getMessageList().hasReceivedMessage());
        receiver1.getMessageList().flushMessages();
        receiver2.getMessageList().flushMessages();

        senderContainer.deactivateComponent("receiver");
        Thread.sleep(1000);

        sender2.sendMessages(NUM_MESSAGES);
        Thread.sleep(3000);
        assertFalse(receiver1.getMessageList().hasReceivedMessage());
        assertTrue(receiver2.getMessageList().hasReceivedMessage());
        receiver1.getMessageList().flushMessages();
        receiver2.getMessageList().flushMessages();

        senderContainer.activateComponent(new ActivationSpec("receiver", receiver1));
        receiverContainer.deactivateComponent("receiver");
        Thread.sleep(1000);

        sender2.sendMessages(NUM_MESSAGES);
        Thread.sleep(3000);
        assertTrue(receiver1.getMessageList().hasReceivedMessage());
        assertFalse(receiver2.getMessageList().hasReceivedMessage());
        receiver1.getMessageList().flushMessages();
        receiver2.getMessageList().flushMessages();
    }

    /**
     * Test case async Stateless message sending.
     * meaning that clustered sender can get the response.
     *
     * @throws Exception
     */
    public void testClusteredStatelessInOnly() throws Exception {
        final StatelessSenderComponent sender1 = new StatelessSenderComponent();
        final StatelessSenderComponent sender2 = new StatelessSenderComponent();
        final ReceiverComponent receiver1 = new ReceiverComponent();
        final ReceiverComponent receiver2 = new ReceiverComponent();
        sender2.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));

        receiverContainer.activateComponent(new ActivationSpec("sender", sender1));
        senderContainer.activateComponent(new ActivationSpec("sender", sender2));
        senderContainer.activateComponent(new ActivationSpec("receiver", receiver1));
        receiverContainer.activateComponent(new ActivationSpec("receiver", receiver2));
        Thread.sleep(1000);

        sender2.sendMessages(NUM_MESSAGES);
        Thread.sleep(3000);

        assertTrue(receiver1.getMessageList().hasReceivedMessage());
        assertTrue(receiver2.getMessageList().hasReceivedMessage());
        receiver1.getMessageList().flushMessages();
        receiver2.getMessageList().flushMessages();

        assertTrue(sender1.getResponseMessageList().hasReceivedMessage());
        assertTrue(sender2.getResponseMessageList().hasReceivedMessage());
    }

    /**
     *
     * todo: resolve this issue with SM...
     *
     * Test case sync Stateless message sending.
     * meaning that clustered sender can get the response.
     * <p>
     * <B>Note: this test will fail , since there is a bug in SM
     * Since the SM overwrite the sync property , and when the flow return response it dosen't
     * know that the orignal message sendsync
     *
     * @throws Exception
     * @see org.openspaces.esb.servicemix.flow.OpenSpacesFlow#doRouting(org.apache.servicemix.jbi.messaging.MessageExchangeImpl)
     *      The second appearance of
     *      if (Boolean.TRUE.equals(me.getProperty(JbiConstants.STATELESS_CONSUMER)) && !isSynchronous(me))
     *      isSynchronous(me) - will always return false. though it supposed to return true when the orignal message sent sync.
     *
     *      </B>
     */
//    public void testClusteredStatelessSyncInOnly() throws Exception {
//        final StatelessSenderComponent sender1 = new StatelessSenderComponent();
//        final StatelessSenderComponent sender2 = new StatelessSenderComponent();
//        final ReceiverComponent receiver1 = new ReceiverComponent();
//        final ReceiverComponent receiver2 = new ReceiverComponent();
//        sender2.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));
//
//        receiverContainer.activateComponent(new ActivationSpec("sender", sender1));
//        senderContainer.activateComponent(new ActivationSpec("sender", sender2));
//        senderContainer.activateComponent(new ActivationSpec("receiver", receiver1));
//        receiverContainer.activateComponent(new ActivationSpec("receiver", receiver2));
//        Thread.sleep(1000);
//
//        sender2.sendMessages(10, true);
//        Thread.sleep(3000);
//
//        assertTrue(receiver1.getMessageList().hasReceivedMessage());
//        assertTrue(receiver2.getMessageList().hasReceivedMessage());
//
//        receiver1.getMessageList().flushMessages();
//        receiver2.getMessageList().flushMessages();
//
//        assertFalse(sender1.getResponseMessageList().hasReceivedMessage());
//        assertTrue(sender2.getResponseMessageList().hasReceivedMessage());
//
//    }

}
