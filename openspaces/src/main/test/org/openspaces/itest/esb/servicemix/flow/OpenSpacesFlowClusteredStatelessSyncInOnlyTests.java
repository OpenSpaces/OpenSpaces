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

/**
 * @author yitzhaki
 */
public class OpenSpacesFlowClusteredStatelessSyncInOnlyTests extends OpenSpacesFlowAbstractTest {

    //todo: delete this empty test after resolving the following issue 
     public void test() throws Exception {

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
//    public void test() throws Exception {
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
