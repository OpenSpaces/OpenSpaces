package org.openspaces.itest.esb.mule.seda;

import org.mule.api.MuleMessage;
import org.openspaces.itest.esb.mule.AbstractMuleTests2;

/**
 * @author yitzhaki
 */
public class PersistentSedaTests extends AbstractMuleTests2 {

    protected String getSpaceName() {
        return "muleSedaPersistent";
    }

    public void xtestSimpleSedaHandling() throws Exception {
        muleClient.dispatch("vm://test1", "testme", null);

        MuleMessage message = muleClient.request("vm://test3", 5000000);
        assertEquals("testme", message.getPayload());
    }

    @Override
    protected String getConfigResources() {
        return "org/openspaces/itest/esb/mule/seda/persistent.xml";
    }
}