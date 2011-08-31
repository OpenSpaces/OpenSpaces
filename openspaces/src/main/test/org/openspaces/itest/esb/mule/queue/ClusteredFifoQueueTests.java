package org.openspaces.itest.esb.mule.queue;

import org.openspaces.esb.mule.queue.OpenSpacesFifoQueueObject;
import org.openspaces.esb.mule.queue.OpenSpacesQueueMessageDispatcher;
import org.openspaces.itest.esb.mule.AbstractMuleTests;

/**
 * @author kimchy
 */
public class ClusteredFifoQueueTests extends AbstractMuleTests {


    private static int OBJECTS = 5;
    public void testClusteredSimpleQueueHandling() throws Exception {
        for (int i = 0; i < OBJECTS; i++) {
            
            OpenSpacesFifoQueueObject request = new OpenSpacesFifoQueueObject();
            request.setPayload("testme" +i);
            request.setEndpointURI("test1");
            gigaSpace.write(request);
        }

        for (int i = 0; i < OBJECTS; i++) {

            
            OpenSpacesFifoQueueObject responseTemplate = new OpenSpacesFifoQueueObject();
            responseTemplate.setEndpointURI("test1" + OpenSpacesQueueMessageDispatcher.DEFAULT_RESPONSE_QUEUE);
            
            OpenSpacesFifoQueueObject response = gigaSpace.take(responseTemplate,5000);
          
            assertEquals("testme" + i +"Appender1Appender2", response.getPayload());
        }
    }
    @Override
    protected String getConfigResources() {
        return "org/openspaces/itest/esb/mule/queue/clustered-fifo.xml";
    }
}
