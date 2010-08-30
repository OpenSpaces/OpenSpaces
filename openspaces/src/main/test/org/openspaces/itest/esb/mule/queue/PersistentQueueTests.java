package org.openspaces.itest.esb.mule.queue;

import org.mule.api.MuleMessage;
import org.openspaces.itest.esb.mule.AbstractMuleTests;

/**
 * Checks working withe os-queue in persistent mode.
 *
 * @author yitzhaki
 */
public class PersistentQueueTests extends AbstractMuleTests {

    protected String[] getConfigLocations() {
        return new String[]{"org/openspaces/itest/esb/mule/queue/persistent.xml"};
    }

    protected String getSpaceName() {
        return "muleQueuePersistent";
    }

    public void xtestSimpleQueueHandling() throws Exception {
        muleClient.dispatch("os-queue://test1", "testme", null);
        MuleMessage message = muleClient.request("os-queue://test2", 5000);
        assertEquals("testmeAppender1", message.getPayload());
    }
}