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

package org.openspaces.itest.pu.container.integrated.simple;

import junit.framework.TestCase;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;

/**
 * @author kimchy
 */
public class SimpleIntegratedContainerTests extends TestCase {

    private IntegratedProcessingUnitContainer integratedContainer;


    protected void setUp() throws Exception {
        IntegratedProcessingUnitContainerProvider factory = new IntegratedProcessingUnitContainerProvider();
        factory.addConfigLocation("/org/openspaces/itest/pu/container/integrated/simple/*.xml");

        BeanLevelProperties beanLevelProperties = new BeanLevelProperties();
        beanLevelProperties.getBeanProperties("testBean1").setProperty("prop.value", "testme");
        factory.setBeanLevelProperties(beanLevelProperties);

        integratedContainer = (IntegratedProcessingUnitContainer) factory.createContainer();
    }

    protected void tearDown() throws Exception {
        integratedContainer.close();
    }

    public void testCorrectAssembly() {
        TestBean1 testBean1 = (TestBean1) integratedContainer.getApplicationContext().getBean("testBean1");
        // verify that the name got injected into the value ${prop.value}
        assertEquals("testme", testBean1.getValue());
        // verify that the merged config properties under the testBean1 name got injected correctly
        assertNotNull(testBean1.getBeanLevelProperties());
        assertEquals("testme", testBean1.getBeanLevelProperties().getProperty("prop.value"));

        TestBean2 testBean2 = (TestBean2) integratedContainer.getApplicationContext().getBean("testBean2");
        assertEquals("test", testBean2.getValue());
        assertNotNull(testBean2.getBeanLevelProperties());
        assertNotNull(testBean2.getMergedProperties());
        // verify that the prop.value property that was assigned to testBean1 is not part of testBean2
        assertNull(testBean2.getMergedProperties().getProperty("prop.value"));
    }
}
