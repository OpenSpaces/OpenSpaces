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

package org.openspaces.itest.esb.mule;

import junit.framework.TestCase;
import net.jini.core.lease.Lease;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;

/**
 * Test the ability to run PU with mule imbedded in it.
 *
 * @author yitzhaki
 */
public class IntegratedPUMuleEmbedTests extends TestCase {

    public void testTakeSingleFromSpace() throws Exception {
        IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();
        provider.addConfigLocation("/org/openspaces/itest/esb/mule/puembedmuleref2.xml");
        IntegratedProcessingUnitContainer container = (IntegratedProcessingUnitContainer) provider.createContainer();

        GigaSpace gigaSpace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://*/*/space").lookupGroups(System.getProperty("user.name")).space()).gigaSpace();

        int numberOfMsgs = 22;
        for (int i = 0; i < numberOfMsgs; i++) {
            Message message = new Message("Hello World " + i, false);
            gigaSpace.write(message);
        }

        //blocking wait untill the mule writes back the messages to the space after reading them.
        for (int i = 0; i < numberOfMsgs; i++) {
            Message template = new Message("Hello World " + i, true);
            Message message = gigaSpace.take(template, Lease.FOREVER);
            assertNotNull(message);
        }
        assertEquals(0, gigaSpace.count(new Message()));

        container.close();
    }
}