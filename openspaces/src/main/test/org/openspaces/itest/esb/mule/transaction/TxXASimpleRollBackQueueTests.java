package org.openspaces.itest.esb.mule.transaction;

import org.mule.api.MuleMessage;
import org.openspaces.itest.esb.mule.AbstractMuleTests;

/**
 *
 * Running os-queue within JTA transaction.
 * The transaction rollback at the first invocation and commit successfully in the second invocation.
 *
 * @author yitzhaki
 */
public class TxXASimpleRollBackQueueTests extends AbstractMuleTests {

    public void testSimpleQueueHandling() throws Exception {
        muleClient.dispatch("os-queue://test1", "testme", null);

        MuleMessage message = muleClient.request("os-queue://test3", 5000);
        assertEquals("testmeAppender1Appender2", message.getPayload());
    }

    @Override
    protected String getConfigResources() {
        return "org/openspaces/itest/esb/mule/transaction/tx-xa-simple-roolback.xml";
    }
}