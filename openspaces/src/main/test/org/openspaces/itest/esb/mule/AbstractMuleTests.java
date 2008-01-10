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
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.extras.client.MuleClient;
import org.mule.umo.UMOManagementContext;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

/**
 * Convenient superclass for tests depending on a Spring context.
 *
 * @author yitzhaki
 */
public abstract class AbstractMuleTests extends TestCase {

    protected UMOManagementContext umoManagementContext;

    protected SpringXmlConfigurationBuilder builder;

    protected GigaSpace gigaSpace;

    protected MuleClient muleClient;

    protected void setUp() throws Exception {
        createApplicationContext(getConfigLocations());
        gigaSpace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://*/*/space").lookupGroups(System.getProperty("user.name")).space()).gigaSpace();
        umoManagementContext.start();
    }

    protected void createApplicationContext(String[] locations) throws Exception {
        builder = new SpringXmlConfigurationBuilder();
        umoManagementContext = builder.configure(locations);
        muleClient = new MuleClient(umoManagementContext);
    }

    protected void tearDown() throws Exception {
        umoManagementContext.dispose();
    }

    protected abstract String[] getConfigLocations();
}