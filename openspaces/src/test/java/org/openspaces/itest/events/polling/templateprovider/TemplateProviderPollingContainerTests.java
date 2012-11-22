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

package org.openspaces.itest.events.polling.templateprovider;

import junit.framework.Assert;

import org.openspaces.core.GigaSpace;
import org.openspaces.itest.events.pojos.MockPojo;
import org.openspaces.itest.utils.TestUtils;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author Itai Frenkel
 * @since 9.1.1
 */
public class TemplateProviderPollingContainerTests extends AbstractDependencyInjectionSpringContextTests {


    protected GigaSpace gigaSpace;
    protected TestEventListener eventListener;


    public TemplateProviderPollingContainerTests() {
      //spring context need to inject this.gigaSpace and this.eventListener
        setPopulateProtectedVariables(true);
    }
    
    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/events/polling/templateprovider/polling-templateprovider.xml"};
    }


    public void testTemplateProvider() throws Exception{
        eventListener.reset();
        assertEquals(0, eventListener.getMessageCounter());
        final MockPojo[] entries = new MockPojo[] { new MockPojo(false, 1), new MockPojo(false, 2), new MockPojo(false, 3), new MockPojo(false, 4)  };
        gigaSpace.writeMultiple(entries);
        TestUtils.repetitive(new Runnable() {

            @Override
            public void run() {
                Assert.assertEquals(entries.length, eventListener.getMessageCounter());
            }

        }, 10000);
    }

}