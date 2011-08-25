package org.openspaces.itest.esb.mule.queue;

import org.mule.api.MuleMessage;
import org.openspaces.itest.esb.mule.AbstractMuleTests;

/**
 * @author kimchy
 */
public class ClusteredSimpleQueueTests extends AbstractMuleTests {

    public void testClusteredSimpleQueueHandling() throws Exception {
        muleClient.dispatch("os-queue://test1", "testme", null);

        MuleMessage message = muleClient.request("os-queue://test3", 500000);
        assertEquals("testmeAppender1Appender2", message.getPayload());
    }

    @Override
    protected String getConfigResources() {
        return "org/openspaces/itest/esb/mule/queue/clustered-simple.xml";
    }
}
