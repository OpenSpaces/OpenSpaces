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

import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

/**
 * Convenient superclass for tests depending on a Mule context.
 *
 * @author yitzhaki
 */
public abstract class AbstractMuleTests extends FunctionalTestCase {

    protected static final int TIMEOUT = 5000;

    //protected MuleContext muleContext;

    //protected SpringXmlConfigurationBuilder builder;

    protected GigaSpace gigaSpace;

    protected MuleClient muleClient;
    
    protected abstract String[] getConfigLocations();
    
    protected String getConfigResources(){
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        muleClient = new MuleClient(muleContext);
        gigaSpace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://*/*/" + getSpaceName()).lookupGroups(System.getProperty("user.name")).space()).gigaSpace();
        muleContext.start();
    }
    
    protected String getSpaceName() {
        return "space";
    }

    protected void createApplicationContext() throws Exception {
        //MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
        //SpringXmlConfigurationBuilder builder = new SpringXmlConfigurationBuilder(locations);
        //muleContext = muleContextFactory.createMuleContext(builder);
    }

   /* protected void tearDown() throws Exception {
        muleContext.dispose();
    }*/
}