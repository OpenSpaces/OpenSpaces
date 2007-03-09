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
