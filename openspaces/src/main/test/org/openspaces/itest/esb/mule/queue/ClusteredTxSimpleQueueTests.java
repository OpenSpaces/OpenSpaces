package org.openspaces.itest.esb.mule.queue;

import org.mule.api.MuleMessage;
import org.openspaces.itest.esb.mule.AbstractMuleTests;

/**
 * Testing runing os-queue within trnasaction within local OpenSapce transaction.
 * The transaction commit succesufly.
 *
 * @author yitzhaki
 */
public class ClusteredTxSimpleQueueTests extends AbstractMuleTests {

    public void testSimpleQueueHandling() throws Exception {
        muleClient.dispatch("os-queue://test1", "testme", null);

        MuleMessage message = muleClient.request("os-queue://test3", 5000);
        System.out.println(gigaSpace.read(null));
        assertEquals("testmeAppender1Appender2", message.getPayload());
    }

    @Override
    protected String getConfigResources() {
        return "org/openspaces/itest/esb/mule/queue/clustered-tx-simple.xml";
    }
}