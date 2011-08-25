package org.openspaces.itest.esb.mule.queue;

import org.openspaces.esb.mule.queue.OpenSpacesQueueMessageDispatcher;
import org.openspaces.esb.mule.queue.OpenSpacesQueueObject;
import org.openspaces.itest.esb.mule.AbstractMuleTests;

/**
 * @author anna
 * @since 8.0.4
 */
public class NullRequestResponseQueueTests extends AbstractMuleTests {

    public void testSimpleQueueHandling() throws Exception {

        OpenSpacesQueueObject request = new OpenSpacesQueueObject();
        request.setPayload("testme");
        request.setEndpointURI("null.response.in");
        gigaSpace.write(request);
        
        OpenSpacesQueueObject responseTemplate = new OpenSpacesQueueObject();
        responseTemplate.setEndpointURI("null.response.in" + OpenSpacesQueueMessageDispatcher.DEFAULT_RESPONSE_QUEUE);
        
        OpenSpacesQueueObject response = gigaSpace.take(responseTemplate,5000);
        assertNull(response.getPayload());
       
    }

    @Override
    protected String getConfigResources() {
        return "org/openspaces/itest/esb/mule/queue/null-request-response-queue.xml";
    }
}
