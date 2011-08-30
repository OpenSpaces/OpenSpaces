package org.openspaces.esb.mule.queue;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceRouting;

@SpaceClass(fifoSupport=FifoSupport.ALL)
public class OpenSpacesFifoQueueObject extends OpenSpacesQueueObject {
    private static final long serialVersionUID = 1L;

    public OpenSpacesFifoQueueObject() {
        super();
    }

    /**
     * to keep the fifo ordering the whole queue must be placed in the same partition.
     * therefore the routing is done on the endpoint uri
     */
    @Override
    @SpaceRouting
    public String getEndpointURI() {
        return super.getEndpointURI();
    }
}
