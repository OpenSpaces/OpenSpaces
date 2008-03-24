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
package org.openspaces.itest.esb.servicemix;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Testing running servicemix within a PU.
 *
 * @author yitzhaki
 */
public class PUServicemixTests extends AbstractDependencyInjectionSpringContextTests {


    protected String[] getConfigLocations() {
        return new String[]{"org/openspaces/itest/esb/servicemix/puservicemix.xml"};
    }

    public void test() {
        GigaSpace gigaSpace =
                new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://*/*/space").lookupGroups(System.getProperty("user.name")).space()).gigaSpace();
        for (int i = 0; i < 100; i++) {
            Message msg = new Message("hello " + i, false);
            gigaSpace.write(msg);
        }
        for (int i = 0; i < 100; i++) {
            Message msg = new Message("hello " + i, true);
            Message message = gigaSpace.read(msg, Long.MAX_VALUE);
            assertNotNull(message);

        }
        int count = gigaSpace.count(new Message());
        assertEquals(count, 0);
    }
}
