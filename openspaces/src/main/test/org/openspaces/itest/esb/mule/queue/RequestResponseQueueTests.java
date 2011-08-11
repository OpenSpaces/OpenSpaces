package org.openspaces.itest.esb.mule.queue;

import org.mule.api.MuleMessage;
import org.openspaces.itest.esb.mule.AbstractMuleTests;

/**
 * @author anna
 * @since 8.0.4
 */
public class RequestResponseQueueTests extends AbstractMuleTests {

    public void testSimpleQueueHandling() throws Exception {

        MuleMessage result = muleClient.send("os-queue://bookingflow.in", "testme", null, 5000);
        System.out.println(gigaSpace.read(null));
        assertEquals("testmeBookingServiceInternalBookingServiceExternalBookingService", result.getPayload());
       
    }

    @Override
    protected String getConfigResources() {
        return "org/openspaces/itest/esb/mule/queue/request-response-queue.xml";
    }
}
