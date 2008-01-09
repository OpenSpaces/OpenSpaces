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
import org.mule.config.ConfigurationException;
import org.mule.config.builders.MuleXmlConfigurationBuilder;
import org.mule.umo.UMOManagementContext;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.springframework.beans.factory.BeanDefinitionStoreException;

/**
 * Convenient superclass for tests depending on a Spring context.
 *
 * @author yitzhaki
 */
public abstract class AbstractMuleTests extends TestCase {

    protected UMOManagementContext umoManagementContext;

    protected MuleXmlConfigurationBuilder builder;

    protected GigaSpace gigaSpace;


    protected void setUp() throws Exception {
        createApplicationContext(getConfigLocations());
        gigaSpace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://*/*/space").lookupGroups(System.getProperty("user.name")).space()).gigaSpace();
        umoManagementContext.start();
    }

    protected void createApplicationContext(String[] locations) {
        builder = new MuleXmlConfigurationBuilder();
        try {
            umoManagementContext = builder.configure(locations);
        } catch (ConfigurationException e) {
            throw new BeanDefinitionStoreException("Failed to load configuration");
        }
    }

    protected void tearDown() throws Exception {
        umoManagementContext.dispose();
    }

    protected abstract String[] getConfigLocations();
}