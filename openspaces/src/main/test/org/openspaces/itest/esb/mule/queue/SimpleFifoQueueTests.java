package org.openspaces.itest.esb.mule.queue;

import org.mule.api.MuleMessage;
import org.openspaces.itest.esb.mule.AbstractMuleTests;

/**
 * @author kimchy
 */
public class SimpleFifoQueueTests extends AbstractMuleTests {

    private static int OBJECTS = 5;
    public void testClusteredSimpleQueueHandling() throws Exception {
        for (int i = 0; i < OBJECTS; i++) {
            
            muleClient.dispatch("os-queue://test1", "testme" + i, null);
        }

        for (int i = 0; i < OBJECTS; i++) {
            MuleMessage message = muleClient.request("os-queue://test2", 5000);
            assertEquals("testme" + i +"Appender1", message.getPayload());
        }
    }

    @Override
    protected String getConfigResources() {
        return "org/openspaces/itest/esb/mule/queue/simple-fifo.xml";
    }
}
