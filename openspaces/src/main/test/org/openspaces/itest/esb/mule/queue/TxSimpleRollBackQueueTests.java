package org.openspaces.itest.esb.mule.queue;

import org.mule.api.MuleMessage;
import org.openspaces.itest.esb.mule.AbstractMuleTests;

/**
 * Running os-queue within OpenSpace local transaction.
 * The transaction rollback at the first invocation and commit successfully in the second invocation.
 *
 * @author yitzhaki
 */
public class TxSimpleRollBackQueueTests extends AbstractMuleTests {

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/esb/mule/queue/tx-simple-roolback.xml"};
    }

    public void testSimpleQueueHandling() throws Exception {
        muleClient.dispatch("os-queue://test1", "testme", null);

        MuleMessage message = muleClient.request("os-queue://test3", 5000000);
        assertEquals("testmeAppender1Appender2", message.getPayload());
    }
}