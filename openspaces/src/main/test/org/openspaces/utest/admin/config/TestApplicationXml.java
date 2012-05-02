/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.utest.admin.config;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.openspaces.admin.application.ApplicationDeployment;
import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.space.SpaceDeployment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Tests that reading an application.xml is the same as using fluent admin API. 
 * @author itaif
 * @since 9.0.1
 */

public class TestApplicationXml extends TestCase {

    private final String TEST_APPLICATION_XML = "/org/openspaces/utest/admin/config/test-application.xml";
    
    public void testRelocationSchema() {
        Assert.assertEquals(createApplicationWithAdminApi(),createApplicationFromXml());
    }

    private ApplicationConfig createApplicationFromXml() {
        
        final ApplicationContext context = new ClassPathXmlApplicationContext(TEST_APPLICATION_XML);
        final ApplicationConfig applicationConfig = context.getBean(org.openspaces.admin.application.config.ApplicationConfig.class);
        Assert.assertNotNull(applicationConfig);
        return applicationConfig;
    }

    private ApplicationConfig createApplicationWithAdminApi() {
        ApplicationDeployment applicationDeployment = new ApplicationDeployment("test-application")
        .addProcessingUnitDeployment(new SpaceDeployment("test-space").addDependency("test-pu"))
        .addProcessingUnitDeployment(new ProcessingUnitDeployment("test-pu.jar"));
        return applicationDeployment.create();
    }
}