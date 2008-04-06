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
 * Simple test that check sending one message via openspacesflow.
 *
 * @author yitzhaki
 */
public class OpenSpacesFlowSimpleTests extends TestCase {

    private static final int NUM_MESSAGES = 10;

    protected JBIContainer container = new JBIContainer();
    protected SenderComponent sender;
    protected ReceiverComponent receiver;

    public void test() throws Exception {
        sender.sendMessages(NUM_MESSAGES);
        receiver.getMessageList().assertMessagesReceived(NUM_MESSAGES);
    }

    protected void setUp() throws Exception {
        container.setFlowName("openspaces");
        container.setEmbedded(true);
        OpenSpacesFlow osflow = new OpenSpacesFlow();
        GigaSpace gigaSpace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("/./space").lookupGroups(System.getProperty("user.name")).space()).gigaSpace();
        osflow.setGigaSpace(gigaSpace);
        container.setFlow(osflow);
        container.init();
        container.start();
        receiver = new ReceiverComponent();
        sender = new SenderComponent();
        sender.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));

        container.activateComponent(new ActivationSpec("sender", sender));
        container.activateComponent(new ActivationSpec("receiver", receiver));
    }

    protected void tearDown() throws Exception {
        container.shutDown();
    }
}