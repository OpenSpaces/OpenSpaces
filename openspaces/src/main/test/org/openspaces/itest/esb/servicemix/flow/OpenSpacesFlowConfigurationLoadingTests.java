package org.openspaces.itest.esb.servicemix.flow;

import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.openspaces.core.GigaSpace;
import org.openspaces.itest.esb.servicemix.Message;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * Testing <code>org.openspaces.esb.servicemix.OpenSpacesInBinding</code> and
 * <code>org.openspaces.esb.servicemix.OpenSpacesOutBinding</code> within openspaces flow.
 */
public class OpenSpacesFlowConfigurationLoadingTests extends SpringTestSupport {

    protected Receiver receiver;

    private GigaSpace gigaSpace;

    protected void setUp() throws Exception {
        super.setUp();
        gigaSpace = (GigaSpace) getBean("gigaSpace");
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("/org/openspaces/itest/esb/servicemix/flow/flow.xml");
    }

    public void test() {
        for (int i = 0; i < 10; i++) {
            Message msg = new Message("hello " + i, false);
            gigaSpace.write(msg);
        }
        for (int i = 0; i < 10; i++) {
            Message msg = new Message("hello " + i, true);
            Message message = gigaSpace.read(msg, Long.MAX_VALUE);
            assertNotNull(message);
        }
        int count = gigaSpace.count(new Message());
        assertEquals(0, count);
    }
}