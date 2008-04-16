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
import org.apache.servicemix.jbi.container.JBIContainer;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.esb.servicemix.flow.OpenSpacesFlow;

/**
 * Base class for testing sending messages within clustered flows .
 *
 * @author yitzhaki
 */
public abstract class OpenSpacesFlowAbstractTest extends TestCase {

    protected static final int NUM_MESSAGES = 10;

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
        Thread.sleep(3000);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        senderContainer.shutDown();
        receiverContainer.shutDown();
    }

}
