/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.esb.mule.queue;

import org.mule.providers.AbstractMessageRequester;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.openspaces.core.SpaceInterruptedException;

/**
 * @author kimchy
 */
public class OpenSpacesQueueMessageRequestor extends AbstractMessageRequester {

    private final OpenSpacesQueueConnector connector;

    private Object template;

    public OpenSpacesQueueMessageRequestor(UMOImmutableEndpoint endpoint) {
        super(endpoint);
        this.connector = (OpenSpacesQueueConnector) endpoint.getConnector();
    }

    /**
     * Make a specific request to the underlying transport
     *
     * @param timeout the maximum time the operation should block before returning.
     *                The call should return immediately if there is data available. If
     *                no data becomes available before the timeout elapses, null will be
     *                returned
     * @return the result of the request wrapped in a UMOMessage object. Null will be
     *         returned if no data was available
     * @throws Exception if the call to the underlying protocol causes an exception
     */
    protected UMOMessage doRequest(long timeout) throws Exception {
        try {
            UMOMessage message = null;
            if (logger.isDebugEnabled()) {
                logger.debug("Waiting for a message on " + endpoint.getEndpointURI().getAddress());
            }
            try {
                InternalQueueEntry entry = (InternalQueueEntry) connector.getGigaSpaceObj().take(template, timeout);
                if (entry != null) {
                    message = entry.message;
                }
            } catch (SpaceInterruptedException e) {
                logger.debug("Failed to receive message from queue on interruption: " + endpoint.getEndpointURI());
            }
            if (message != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Message received: " + message);
                }
                return message;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No event received after " + timeout + " ms");
                }
                return null;
            }
        }
        catch (Exception e) {
            throw e;
        }
    }

    protected void doDispose() {
        // template method
    }

    protected void doConnect() throws Exception {
        InternalQueueEntry internalTemplate = new InternalQueueEntry();
        internalTemplate.endpointURI = endpoint.getEndpointURI().getAddress();
        internalTemplate.setFifo(connector.isFifo());
        if (connector.isPersistent()) {
            internalTemplate.makePersistent();
        } else {
            internalTemplate.makeTransient();
        }
        template = connector.getGigaSpaceObj().snapshot(internalTemplate);
    }

    protected void doDisconnect() throws Exception {
        // template method
    }

}